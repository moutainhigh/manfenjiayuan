package com.manfenjiayuan.pda_supermarket.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.OrderPayInfo;
import com.manfenjiayuan.pda_supermarket.cashier.CashierAgent;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderEntity;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderItemEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.PosOrderService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.rxapi.http.RxHttpManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

import rx.Subscriber;


/**
 * 上传数据，确保POS机数据能正确的上传到云端
 * <ol>
 * <li>多台POS机如果同时有大量数据提交到后台，可能会影响性能，所以订单同步不需要要实时同步</li>
 * <li>定时任务每隔10分钟同步一次。{@link TimeTaskManager#syncPosOrderTimer}</li>
 * <li>如果没有订单需要同步，则取消定时器</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DataUploadManager {
    public static final int NA = 0;
    public static final int POS_ORDER = 1;//收银订单／外部订单

    private int rollback = -1;
    private static final int MAX_ROLLBACK = 5;
    private boolean bSyncInProgress = false;//是否正在同步
    private int queue = NA;//默认同步所有数据

    public static final int MAX_SYNC_ORDER_PAGESIZE = 4;
    protected PageInfo mOrderPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_ORDER_PAGESIZE);//翻页
    protected String orderSqlWhere;


    private static DataUploadManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static DataUploadManager getInstance() {
        if (instance == null) {
            synchronized (DataUploadManager.class) {
                if (instance == null) {
                    instance = new DataUploadManager();
                }
            }
        }
        return instance;
    }


    /**
     * 下载更新POS数据库
     */
    public synchronized void syncDefault() {
        sync(1);
    }

    public void sync(int step) {
        queue |= step;
        if (bSyncInProgress) {
            rollback++;
            ZLogger.df(String.format("正在同步POS数据..., rollback=%d/%d", rollback, MAX_ROLLBACK));
            EventBus.getDefault().post(new UploadSyncManagerEvent(UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));

            //自动恢复同步
            if (rollback >= MAX_ROLLBACK) {
                bSyncInProgress = false;
            }
            return;
        }

        processQueue();
    }

    /**
     * 同步
     */
    private void processQueue() {
        ZLogger.d(String.format("queue ＝ %d", queue));
        rollback = -1;
        this.bSyncInProgress = true;

        if ((queue & POS_ORDER) == POS_ORDER) {
            uploadPosOrders();
        } else {
            onNotifyCompleted("没有上传任务待执行");
        }
    }


    private void onNotifyNext(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.df(message);
        }
        processQueue();
    }

    /**
     * 完成
     */
    private void onNotifyCompleted(String message) {
        if (!StringUtils.isEmpty(message)) {
            ZLogger.df(message);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new UploadSyncManagerEvent(UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    public static class UploadSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
        public static final int EVENT_ID_SYNC_DATA_ERROR = 0X12;//同步失败
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X13;//同步结束

        private int eventId;

        public UploadSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public int getEventId() {
            return eventId;
        }
    }



    /**
     * 上传未同步的已完成订单
     * */
    public void uploadPosOrders(){
        queue ^= POS_ORDER;

        List<PosOrderEntity> orderEntities = DataManagerHelper.getSyncPosOrders(PosOrderEntity.SYNC_STATUS_NONE);
        if (orderEntities != null && orderEntities.size() > 0) {
            stepUploadPosOrder(orderEntities.get(0));
        } else {
            ZLogger.df("没有找到未同步的已完成订单");
            uploadErrorPosOrders(true);
        }
    }

    /**
     * 上传POS订单
     * 根据上一次同步游标同步订单数据，上传同步失败的订单，相当于进行一次重试操作。
     */
    public synchronized void uploadErrorPosOrders(boolean isReload) {
        if (isReload) {
            mOrderPageInfo = new PageInfo(1, MAX_SYNC_ORDER_PAGESIZE);
            String orderStartCursor = DataManagerHelper.getPosOrderStartCursor();
            //上传未同步并且已完成的订单
            orderSqlWhere = String.format("updatedDate >= '%s' and sellerId = '%d' " +
                            "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                    orderStartCursor, MfhLoginService.get().getSpid(),
                    PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                    PosOrderEntity.SYNC_STATUS_ERROR);
        }

        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(orderSqlWhere, mOrderPageInfo);
        ZLogger.df(String.format("查询到 %d 个同步失败的POS订单，" +
                        "当前页数 %d/%d,每页最多 %d 个订单",
                mOrderPageInfo.getTotalCount(), mOrderPageInfo.getPageNo(),
                mOrderPageInfo.getTotalPage(), mOrderPageInfo.getPageSize()));

        if (orderEntities == null || orderEntities.size() < 1) {
            onNotifyNext("没有POS订单需要上传。");
        }
//        else if (orderEntities.size() == 1) {
//            stepUploadPosOrder(orderEntities.get(0));
//        }
        else {
            batchUploadPosOrder(orderEntities);
        }
    }


    /**
     * 生成订单同步数据结构
     */
    private JSONObject wrapperOrder(PosOrderEntity orderEntity) {
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
        order.put("outerNo", orderEntity.getOuterTradeNo());//外部订单编号（外部平台订单组货功能特有）
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
        if (payWrapper != null) {
            order.put("payWays", payWrapper.getPayWays());
            order.put("disAmount", payWrapper.getRuleDiscount()); //优惠金额
            //卡券核销
            order.put("couponsIds", payWrapper.getCouponsIds());
            order.put("ruleIds", payWrapper.getRuleIds());
            order.put("payType", payWrapper.getPayType());
            Double amount = orderEntity.getFinalAmount() - payWrapper.getRuleDiscount();
            order.put("amount", amount);//负数表示退单
            if (amount >= 0.01) {
                order.put("score", amount / 2);
            } else {
                order.put("score", 0D);
            }
        } else {
            order.put("amount", orderEntity.getFinalAmount());//实际支付金额
        }

        return order;
    }

    /**
     * 批量上传POS订单
     */
    private void batchUploadPosOrder(List<PosOrderEntity> orderEntities) {
        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步POS订单数据。");
            return;
        }

        if (orderEntities == null || orderEntities.size() < 1) {
            onNotifyNext("没有POS订单需要上传");
            return;
        }

        Date newCursor = null;
        JSONArray orders = new JSONArray();
        for (PosOrderEntity orderEntity : orderEntities) {
            //保存最大时间游标
            if (newCursor == null || orderEntity.getUpdatedDate() == null
                    || newCursor.compareTo(orderEntity.getUpdatedDate()) <= 0) {
                newCursor = orderEntity.getUpdatedDate();
            }

            orders.add(wrapperOrder(orderEntity));
        }

        final Date finalNewCursor = newCursor;
        RxHttpManager.getInstance().batchInOrders(MfhLoginService.get().getCurrentSessionId(),
                orders,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onNotifyNext(String.format("上传POS订单失败: %s", e.toString()));
                    }

                    @Override
                    public void onNext(String s) {
                        SharedPrefesManagerUltimate.setPosOrderLastUpdate(finalNewCursor);
                        if (mOrderPageInfo.hasNextPage()) {
                            mOrderPageInfo.moveToNext();
                            uploadErrorPosOrders(false);
                        } else {
                            onNotifyNext(String.format("上传POS订单数据完成。%s",
                                    SharedPrefesManagerUltimate.getPosOrderLastUpdate()
                            ));
                        }
                    }
                });
    }

    /**
     * 提交单条订单
     * 适用场景：订单完成后立即上传，上传成功只会影响订单同步状态，不会影响同步游标
     */
    public void stepUploadPosOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null) {
            onNotifyNext("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH) {
            onNotifyNext(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            onNotifyCompleted("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onNotifyCompleted("网络未连接，暂停同步POS订单数据。");
            return;
        }

        ZLogger.df(String.format("准备上传POS订单(%d/%s)", orderEntity.getId(),
                orderEntity.getBarCode()));

        JSONArray orders = new JSONArray();
        orders.add(wrapperOrder(orderEntity));

//            Map<String, String> options = new HashMap<>();
//            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//            options.put("jsonStr", orders.toJSONString());
        RxHttpManager.getInstance().batchInOrders(MfhLoginService.get().getCurrentSessionId(),
                orders,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_ERROR);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);
                        uploadPosOrders();
                    }

                    @Override
                    public void onNext(String s) {
                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        uploadPosOrders();
                    }
                });
    }

    /**
     * 提交遗漏的订单
     */
    private void uploadMissingOrders() {
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(sqlWhere, null);
        if (orderEntities != null && orderEntities.size() > 0) {
            PosOrderEntity orderEntity = orderEntities.get(0);
            stepUploadPosOrder(orderEntity);
        } else {
            onNotifyNext(String.format("上传POS订单数据完成。%s",
                    SharedPrefesManagerUltimate.getPosOrderLastUpdate()
            ));
        }
    }

}
