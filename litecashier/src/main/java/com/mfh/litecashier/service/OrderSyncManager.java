package com.mfh.litecashier.service;


import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.logic.PosOrderItemService;
import com.mfh.litecashier.database.logic.PosOrderService;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * POS-- 订单同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class OrderSyncManager {
    private static final int MAX_SYNC_ORDER_PAGESIZE = 10;
    private boolean bSyncInProgress = false;//是否正在同步

    private static OrderSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static OrderSyncManager get() {
        if (instance == null) {
            synchronized (OrderSyncManager.class) {
                if (instance == null) {
                    instance = new OrderSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 上传POS订单
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            uploadProcess("OrderSync--正在同步POS订单数据...");
            return;
        }

        batchUploadPosOrder();
    }

    /**
     * 上传结束
     */
    private void uploadProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
        EventBus.getDefault().post(new OrderSyncManagerEvent(OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS));
    }

    /**
     * 上传结束
     */
    private void uploadFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new OrderSyncManagerEvent(OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 上传结束失败
     */
    private void uploadFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new OrderSyncManagerEvent(OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED));
    }

    /**
     * 生成订单同步数据结构
     */
    public JSONObject generateOrderJson(PosOrderEntity orderEntity) {
        JSONObject order = new JSONObject();

        order.put("id", orderEntity.getId());
        order.put("barCode", orderEntity.getBarCode());
        order.put("humanId", orderEntity.getHumanId());//
        order.put("status", orderEntity.getStatus());
        order.put("remark", orderEntity.getRemark());
        order.put("bcount", orderEntity.getBcount());
        order.put("adjPrice", orderEntity.getAdjPrice());
        //卡券核销
        order.put("couponsIds", orderEntity.getCouponsIds());
        order.put("ruleIds", orderEntity.getRuleIds());
        order.put("score", orderEntity.getScore());

        if (orderEntity.getRetailAmount() == 0D) {
            order.put("discount", Double.valueOf(String.valueOf(Integer.MAX_VALUE)));
        } else {
            order.put("discount", (orderEntity.getRetailAmount() - orderEntity.getDiscountAmount()) / orderEntity.getRetailAmount());
        }

        order.put("createdBy", orderEntity.getCreatedBy());

//        Date createdDate = orderEntity.getCreatedDate();
        Date createdDate = orderEntity.getUpdatedDate();//使用订单最后更新日期作为订单生效日期
        if (createdDate == null) {
            createdDate = new Date();
        }
        order.put("createdDate", TimeCursor.InnerFormat.format(createdDate));
        order.put("amount", orderEntity.getRetailAmount() - orderEntity.getDiscountAmount() - orderEntity.getCouponDiscountAmount());
        order.put("paystatus", orderEntity.getPaystatus());
        order.put("payType", orderEntity.getPayType());
        order.put("posId", orderEntity.getPosId());//设备编号
        order.put("sellOffice", orderEntity.getSellOffice());//curoffice id
        order.put("sellerId", orderEntity.getSellerId());//spid

        //读取订单明细
        JSONArray items = new JSONArray();
        List<PosOrderItemEntity> posOrderItemEntityList = PosOrderItemService.get().queryAllBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
        for (PosOrderItemEntity orderItemEntity : posOrderItemEntityList) {
            JSONObject item = new JSONObject();

            item.put("goodsId", orderItemEntity.getGoodsId());
            item.put("productId", orderItemEntity.getProductId());
            item.put("skuId", orderItemEntity.getProSkuId());
            item.put("barcode", orderItemEntity.getBarcode());
            item.put("bcount", orderItemEntity.getBcount());
            item.put("price", orderItemEntity.getCostPrice());
            item.put("amount", orderItemEntity.getBcount() * orderItemEntity.getCostPrice());
            item.put("cateType", orderItemEntity.getCateType());//按类目进行账务清分

            items.add(item);
        }
        order.put("items", items);

        return order;
    }

    /**
     * 批量上传POS订单<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadPosOrder() {
        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("OrderSyncManager--会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("OrderSyncManager--网络未连接，暂停同步POS订单数据。");
            return;
        }

        String lastCursor = SharedPreferencesHelper.getUploadOrderLastUpdate();
        uploadProcess(String.format("OrderSyncManager--检查是否有订单需要上传(%s)", lastCursor));

        //上传未同步并且已完成的订单
//        SELECT * FROM tb_pos_order_v2 WHERE updatedDate > '2016-02-17 14:38:43' and sellerId = '134221' and status = '4' and paystatus = '1' ORDER BY updatedDate asc limit 0,10
        String strWhere = String.format("updatedDate > '%s' and sellerId = '%d' and status = '%d' and syncStatus = '%d'",
                lastCursor, MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllAsc(strWhere, new PageInfo(1, MAX_SYNC_ORDER_PAGESIZE));
        if (orderEntityList == null || orderEntityList.size() < 1) {
            uploadFinished(String.format("OrderSyncManager--没有POS订单需要上传(%s)。", lastCursor));
            return;
        }

        uploadProcess(String.format("OrderSyncManager--准备上传 %d 条POS订单(%s)", orderEntityList.size(), lastCursor));

        Date newCursor = null;

        JSONArray orders = new JSONArray();
        for (PosOrderEntity orderEntity : orderEntityList) {
            //保存最大时间游标
            if (newCursor == null || orderEntity.getUpdatedDate() == null || newCursor.compareTo(orderEntity.getUpdatedDate()) <= 0) {
                newCursor = orderEntity.getUpdatedDate();
            }
//            newCursor = orderEntity.getUpdatedDate();

            orders.add(generateOrderJson(orderEntity));
        }

        final Date finalNewCursor = newCursor;
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        ZLogger.df("OrderSyncManager--上传POS订单成功");
                        // 保存批量上传订单时间
                        SharedPreferencesHelper.setPosOrderLastUpdate(finalNewCursor);

                        //需要更新订单流水
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STORE_ORDERFLOW_ENABLED, true);

                        //继续上传订单
                        batchUploadPosOrder();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        uploadFailed(String.format("OrderSyncManager--上传订单失败: %s", errMsg));
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.batchInOrders(orders.toJSONString(), responseCallback);
    }

    /**
     * 单条上传POS订单<br>
     * 订单结束时立刻同步
     */
    public void stepUploadPosOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null){
            uploadFailed("OrderSyncManager--订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH){
            uploadFailed(String.format("OrderSyncManager--订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }
        if (bSyncInProgress) {
            uploadProcess("OrderSyncManager--正在同步POS订单数据...");
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("OrderSyncManager--会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("OrderSyncManager--网络未连接，暂停同步POS订单数据。");
            return;
        }

        uploadProcess(String.format("OrderSyncManager--准备上传POS订单(%d/%s)", orderEntity.getId(),
                orderEntity.getBarCode()));

        JSONArray orders = new JSONArray();
        orders.add(generateOrderJson(orderEntity));

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        //需要更新订单流水
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STORE_ORDERFLOW_ENABLED, true);

                        //继续检查是否还有其他订单需要上传
//                        batchUploadPosOrder();
                        uploadFinished("OrderSyncManager--上传POS订单成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        uploadFailed(String.format("OrderSyncManager--上传订单失败: %s", errMsg));
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.batchInOrders(orders.toJSONString(), responseCallback);
    }

    public class OrderSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_START    = 0X03;//同步数据开始
        public static final int EVENT_ID_SYNC_DATA_PROCESS  = 0X04;//同步数据处理中
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X05;//同步数据结束
        public static final int EVENT_ID_SYNC_DATA_FAILED   = 0X06;//同步数据失败

        private int eventId;
        private Bundle args;//参数

        public OrderSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public OrderSyncManagerEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }

}
