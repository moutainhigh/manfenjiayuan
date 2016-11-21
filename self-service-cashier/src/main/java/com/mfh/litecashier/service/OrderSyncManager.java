package com.mfh.litecashier.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderItemEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.PosOrderApi;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * POS-- 订单同步
 * <ol>
 *     <li>多台POS机如果同时有大量数据提交到后台，可能会影响性能，所以订单同步不需要要实时同步</li>
 *     <li>定时任务每隔10分钟同步一次。{@link TimeTaskManager#syncPosOrderTimer}</li>
 *     <li>如果没有订单需要同步，则取消定时器</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public abstract class OrderSyncManager {
    public static final int MAX_SYNC_ORDER_PAGESIZE = 2;
    protected PageInfo mOrderPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_ORDER_PAGESIZE);//翻页
    protected String orderStartCursor;
    protected String orderSqlWhere;


    /**
     * 获取订单同步时间游标
     * */
    public static String getPosOrderStartCursor() {
        String lastSyncCursor = SharedPreferencesUltimate
                .getText(SharedPreferencesUltimate.PK_S_POSORDER_SYNC_STARTCURSOR);
        ZLogger.df(String.format("上次订单同步时间游标(%s)。", lastSyncCursor));

        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(lastSyncCursor)) {
            //得到指定模范的时间
            try {
                Date lastSyncDate = TimeCursor.InnerFormat.parse(lastSyncCursor);
                Date rightNow = new Date();
                if (lastSyncDate.compareTo(rightNow) > 0) {
                    lastSyncCursor = TimeCursor.InnerFormat.format(rightNow);
//                    SharedPreferencesUltimate.setPosOrderLastUpdate(d2);
                    ZLogger.df(String.format("上次订单同步时间大于当前时间，使用当前时间(%s)。", lastSyncCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        return lastSyncCursor;
    }

    /**
     * 获取订单同步时间游标
     * */
    public static String getPosOrderStartCursor2() {
        Date startDate = null;
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(sqlWhere, null);
        if (orderEntities != null && orderEntities.size() > 0){
            PosOrderEntity orderEntity = orderEntities.get(0);
            startDate = orderEntity.getUpdatedDate();
        }
        String startCursor = TimeUtil.format(startDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
        ZLogger.df(String.format("上次订单同步时间游标(%s)。",startCursor));

        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(startCursor)) {
            //得到指定模范的时间
            try {
                Date d1 = TimeCursor.InnerFormat.parse(startCursor);
                Date rightNow = new Date();
                if (d1.compareTo(rightNow) > 0) {
                    startCursor = TimeCursor.InnerFormat.format(rightNow);
//                    SharedPreferencesUltimate.setPosOrderLastUpdate(d2);
                    ZLogger.df(String.format("上次订单同步时间大于当前时间，使用当前时间(%s)。", startCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        return startCursor;
    }

    /**
     * 生成订单同步数据结构
     */
    public JSONObject generateOrderJson(PosOrderEntity orderEntity) {
        ZLogger.d(JSONObject.toJSONString(orderEntity));
        JSONObject order = new JSONObject();

        order.put("id", orderEntity.getId());
        order.put("barCode", orderEntity.getBarCode());
        order.put("status", orderEntity.getStatus());
        order.put("remark", orderEntity.getRemark());
        order.put("bcount", orderEntity.getBcount());
        order.put("adjPrice", orderEntity.getRetailAmount() - orderEntity.getFinalAmount()); //调价金额
        order.put("paystatus", orderEntity.getPaystatus());
        order.put("subType", orderEntity.getSubType());
        order.put("posId", orderEntity.getPosId());//设备编号
        order.put("sellOffice", orderEntity.getSellOffice());//curoffice id
        order.put("sellerId", orderEntity.getSellerId());//spid
        order.put("humanId", orderEntity.getHumanId());//会员支付
        //由后台计算折扣
//        if (orderEntity.getRetailAmount() == 0D) {
//            order.put("discount", Double.valueOf(String.valueOf(Integer.MAX_VALUE)));
//        } else {
//            order.put("discount", (orderEntity.getRetailAmount() - orderEntity.getDiscountAmount())
//                    / orderEntity.getRetailAmount());
//        }

        order.put("createdBy", orderEntity.getCreatedBy());

        //使用订单最后更新日期作为订单生效日期
        Date createdDate = orderEntity.getUpdatedDate();
        if (createdDate == null) {
            createdDate = new Date();
        }
        order.put("createdDate", TimeUtil.format(createdDate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));

        //读取订单商品明细
        List<PosOrderItemEntity> orderItemEntities = CashierAgent.fetchOrderItems(orderEntity);
        JSONArray items = new JSONArray();
        for (PosOrderItemEntity entity : orderItemEntities) {
            JSONObject item = new JSONObject();
            item.put("goodsId", entity.getGoodsId());
            item.put("productId", entity.getProductId());
            item.put("skuId", entity.getProSkuId());
            item.put("barcode", entity.getBarcode());
            item.put("bcount", entity.getBcount());
            item.put("price", entity.getCostPrice());//原价（零售价）
            item.put("amount", entity.getAmount());
            item.put("factAmount", entity.getFinalAmount());//订单明细的实际折后销售价格
//            item.put("cateType", entity.getCateType());//按类目进行账务清分
            item.put("prodLineId", entity.getProdLineId());//按产品线进行账务清分
            items.add(item);
        }
        order.put("items", items);

        //2016-07-01 上传订单支付记录到后台
        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
        if (payWrapper != null){
            order.put("payWays", payWrapper.getPayWays());
            order.put("disAmount", payWrapper.getRuleDiscount()); //优惠金额
            //卡券核销
            order.put("couponsIds", payWrapper.getCouponsIds());
            order.put("ruleIds", payWrapper.getRuleIds());
            order.put("payType", payWrapper.getPayType());
            Double amount = orderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
            order.put("amount", amount);//负数表示退单
            if (amount >= 0.01){
                order.put("score", amount / 2);
            }
            else{
                order.put("score", 0D);
            }
        }
        else{
            order.put("amount", orderEntity.getFinalAmount());//实际支付金额
        }

        return order;
    }

    /**
     * 单条上传POS订单<br>
     * 订单结束时立刻同步
     */
    public void stepUploadPosOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null){
            ZLogger.df("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH){
            ZLogger.df(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.df("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.df("网络未连接，暂停同步POS订单数据。");
            return;
        }

        ZLogger.df(String.format("准备上传POS订单(%d/%s)", orderEntity.getId(),
                orderEntity.getBarCode()));

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        //继续检查是否还有其他订单需要上传
//                        batchUploadPosOrder();
                        ZLogger.df("上传POS订单成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("上传订单失败: %s", errMsg));
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        JSONArray orders = new JSONArray();
        orders.add(generateOrderJson(orderEntity));
        PosOrderApi.batchInOrders(orders.toJSONString(), responseCallback);
    }

    /**
     * 单条上传POS订单<br>
     * 订单结束时立刻同步
     */
    @Deprecated
    public void stepUploadPosOrder(final List<PosOrderEntity> orderEntities) {
        if (orderEntities == null || orderEntities.size() <= 0){
            ZLogger.df("订单无效，不需要同步...");
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.df("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.df("网络未连接，暂停同步POS订单数据。");
            return;
        }

        ZLogger.df(String.format("准备上传 %d 条POS订单", orderEntities.size()));

        JSONArray orders = new JSONArray();
        final List<PosOrderEntity> syncOrderList = new ArrayList<>();
        for (PosOrderEntity orderEntity : orderEntities) {
            //保存最大时间游标
            if (orderEntity.getStatus() == PosOrderEntity.ORDER_STATUS_FINISH) {
                orders.add(generateOrderJson(orderEntity));
                syncOrderList.add(orderEntity);
            }
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //修改订单同步状态
                        for (PosOrderEntity orderEntity : syncOrderList){
                            orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
//                            orderEntity.setUpdatedDate(new Date());
                            PosOrderService.get().saveOrUpdate(orderEntity);
                        }

                        //继续检查是否还有其他订单需要上传
                        ZLogger.df("上传POS订单成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("上传订单失败: %s", errMsg));
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        PosOrderApi.batchInOrders(orders.toJSONString(), responseCallback);
    }
}
