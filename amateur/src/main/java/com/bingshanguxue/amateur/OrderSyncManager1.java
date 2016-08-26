package com.mfh.litecashier.service;


import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * POS-- 订单同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class OrderSyncManager1 extends OrderSyncManager{
    private boolean bSyncInProgress = false;//是否正在同步

    private static OrderSyncManager1 instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static OrderSyncManager1 get() {
        if (instance == null) {
            synchronized (OrderSyncManager1.class) {
                if (instance == null) {
                    instance = new OrderSyncManager1();
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
            uploadProcess("正在同步POS订单数据...");
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
     * 批量上传POS订单<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadPosOrder() {
        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("网络未连接，暂停同步POS订单数据。");
            return;
        }

        String lastCursor = SharedPreferencesHelper.getUploadOrderLastUpdate();
        uploadProcess(String.format("检查是否有订单需要上传(%s)", lastCursor));

        //上传未同步并且已完成的订单
//        SELECT * FROM tb_pos_order_v2 WHERE updatedDate > '2016-02-17 14:38:43' and sellerId = '134221' and status = '4' and paystatus = '1' ORDER BY updatedDate asc limit 0,10
        String strWhere = String.format("updatedDate > '%s' and sellerId = '%d' " +
                        "and status = '%d' and syncStatus = '%d'",
                lastCursor, MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllAsc(strWhere, new PageInfo(1, MAX_SYNC_ORDER_PAGESIZE));
        if (orderEntityList == null || orderEntityList.size() < 1) {
            uploadFinished(String.format("没有POS订单需要上传(%s)。", lastCursor));
            return;
        }

        uploadProcess(String.format("准备上传 %d 条POS订单(%s)", orderEntityList.size(), lastCursor));

        Date newCursor = null;
        JSONArray orders = new JSONArray();
        for (PosOrderEntity orderEntity : orderEntityList) {
            //保存最大时间游标
            if (newCursor == null || orderEntity.getUpdatedDate() == null
                    || newCursor.compareTo(orderEntity.getUpdatedDate()) <= 0) {
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
                        ZLogger.df("上传POS订单成功");
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
                        uploadFailed(String.format("上传订单失败: %s", errMsg));
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
        if (orderEntity == null) {
            uploadFailed("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH) {
            uploadFailed(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }
        if (bSyncInProgress) {
            uploadProcess("正在同步POS订单数据...");
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("网络未连接，暂停同步POS订单数据。");
            return;
        }

        uploadProcess(String.format("准备上传POS订单(%d/%s)", orderEntity.getId(),
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
                        uploadFinished("上传POS订单成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        uploadFailed(String.format("上传订单失败: %s", errMsg));
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
    public void stepUploadPosOrder(final List<PosOrderEntity> orderEntityList) {
        if (orderEntityList == null) {
            uploadFailed("订单无效，不需要同步...");
            return;
        }
        if (bSyncInProgress) {
            uploadProcess("正在同步POS订单数据...");
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            uploadFailed("网络未连接，暂停同步POS订单数据。");
            return;
        }

        uploadProcess(String.format("准备上传 %d 条POS订单", orderEntityList.size()));

        JSONArray orders = new JSONArray();
        final List<PosOrderEntity> syncOrderList = new ArrayList<>();
        for (PosOrderEntity orderEntity : orderEntityList) {
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
                        for (PosOrderEntity orderEntity : syncOrderList) {
                            orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
                            orderEntity.setUpdatedDate(new Date());
                            PosOrderService.get().saveOrUpdate(orderEntity);
                        }

                        //需要更新订单流水
                        SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_STORE_ORDERFLOW_ENABLED, true);

                        //继续检查是否还有其他订单需要上传
//                        batchUploadPosOrder();
                        uploadFinished("上传POS订单成功");
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        uploadFailed(String.format("上传订单失败: %s", errMsg));
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.batchInOrders(orders.toJSONString(), responseCallback);
    }

    public class OrderSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_START = 0X03;//同步数据开始
        public static final int EVENT_ID_SYNC_DATA_PROCESS = 0X04;//同步数据处理中
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X05;//同步数据结束
        public static final int EVENT_ID_SYNC_DATA_FAILED = 0X06;//同步数据失败

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
