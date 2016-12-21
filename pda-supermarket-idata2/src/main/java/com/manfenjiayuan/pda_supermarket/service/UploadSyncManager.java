package com.manfenjiayuan.pda_supermarket.service;


import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.pda.utils.SharedPrefesManagerUltimate;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.PosOrderService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.PosOrderApi;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;


/**
 * 上传数据，确保POS机数据能正确的上传到云端
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class UploadSyncManager extends OrderSyncManager {
    public interface SyncStep {
        int STANDBY = -1;
        int CASHIER_ORDER            = 0;//收银订单
    }

    private boolean bSyncInProgress = false;//是否正在同步
    //当前同步进度
    private int nextStep = SyncStep.STANDBY;

    private static UploadSyncManager instance = null;

    /**
     * 返回 DataDownloadManager 实例
     *
     * @return
     */
    public static UploadSyncManager getInstance() {
        if (instance == null) {
            synchronized (UploadSyncManager.class) {
                if (instance == null) {
                    instance = new UploadSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 下载更新POS数据库
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            ZLogger.df("正在同步POS数据...");
            nextStep = SyncStep.CASHIER_ORDER;
            return;
        }
        processStep(SyncStep.CASHIER_ORDER, SyncStep.STANDBY);
    }

    public void sync(int step) {
        if (bSyncInProgress) {
            if (nextStep > step) {
                nextStep = step;
            }
            ZLogger.df(String.format("正在同步POS数据,下一步:%d", nextStep));
        }
        else{
            processStep(step, SyncStep.STANDBY);
        }
    }

    /**
     * 下一步
     */
    private void nextStep() {
        processStep(nextStep, nextStep + 1);
    }

    private void processStep(int step, int nextStep) {
        this.nextStep = nextStep;
        this.bSyncInProgress = true;
        ZLogger.df(String.format("step=%d, nextStep=%d", step, nextStep));

        switch (step) {
            case SyncStep.CASHIER_ORDER: {
                uploadPosOrders();
            }
            break;
            default: {
                onCompleted();
            }
            break;
        }
    }


    private void onNext(String message) {
        ZLogger.df(message);
        nextStep();
    }

    private void onError(String message) {
        ZLogger.df(message);
        bSyncInProgress = false;
        EventBus.getDefault().post(new UploadSyncManagerEvent(UploadSyncManagerEvent.EVENT_ID_SYNC_DATA_ERROR));
    }

    private void onCompleted() {
        ZLogger.df("上传POS数据结束");
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
     * 上传POS订单
     */
    public synchronized void uploadPosOrders() {
        try{
            mOrderPageInfo = new PageInfo(1, MAX_SYNC_ORDER_PAGESIZE);
            orderStartCursor = decorateStartCursor(SharedPrefesManagerUltimate.getPosOrderLastUpdate());
            //上传未同步并且已完成的订单
            orderSqlWhere = String.format("updatedDate >= '%s' and sellerId = '%d' " +
                            "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                    orderStartCursor, MfhLoginService.get().getSpid(),
                    PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                    PosOrderEntity.SYNC_STATUS_NONE);

            List<PosOrderEntity> orderEntityList = PosOrderService.get()
                    .queryAllAsc(orderSqlWhere, mOrderPageInfo);
            if (orderEntityList == null || orderEntityList.size() < 1) {
                onNext(String.format("没有收银订单需要上传(%s)。", orderStartCursor));
            }
            else if (orderEntityList.size() == 1){
                stepUploadPosOrder(orderEntityList.get(0));
            }
            else{
                batchUploadPosOrder();
            }
        }
        catch (Exception e){
            onError(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 批量上传POS订单<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadPosOrder() {
        if (!MfhLoginService.get().haveLogined()) {
            onError("会话已失效，暂停同步收银订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onError("网络未连接，暂停同步收银订单数据。");
            return;
        }

        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllAsc(orderSqlWhere, mOrderPageInfo);
        if (orderEntityList == null || orderEntityList.size() < 1) {
            onNext(String.format("没有收银订单需要上传(%s)。", orderStartCursor));
            return;
        }

        ZLogger.df(String.format("查询到 %d 个收银订单需要同步，" +
                        "当前页数 %d/%d,每页最多 %d 个订单(%s)",
                mOrderPageInfo.getTotalCount(), mOrderPageInfo.getPageNo(),
                mOrderPageInfo.getTotalPage(), mOrderPageInfo.getPageSize(), orderStartCursor));

        Date newCursor = null;
        JSONArray orders = new JSONArray();
        for (PosOrderEntity orderEntity : orderEntityList) {
            //保存最大时间游标
            if (newCursor == null || orderEntity.getUpdatedDate() == null
                    || newCursor.compareTo(orderEntity.getUpdatedDate()) <= 0) {
                newCursor = orderEntity.getUpdatedDate();
            }

            orders.add(generateOrderJson(orderEntity));
        }

        final Date finalNewCursor = newCursor;
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        // 保存批量上传订单时间
                        SharedPrefesManagerUltimate.setPosOrderLastUpdate(finalNewCursor);

                        //继续上传订单
                        if (mOrderPageInfo.hasNextPage()){
                            mOrderPageInfo.moveToNext();
                            ZLogger.df("上传收银订单成功");
                            batchUploadPosOrder();
                        }
                        else{
                            //按时间游标同步之后再同步一次未同步订单，避免按时间戳同步遗漏订单。
                            uploadMissingOrders();
//                            onNext(String.format("上传收银订单数据完成。%s",
//                                    SharedPreferencesHelper.getPosOrderLastUpdate()
//                            ));
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        onNext(String.format("上传收银订单失败: %s", errMsg));
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        PosOrderApi.batchInOrders(orders.toJSONString(), responseCallback);
    }

    /**
     * 提交单条订单
     * */
    public void stepUploadPosOrder(final PosOrderEntity orderEntity) {
        if (orderEntity == null){
            onNext("订单无效，不需要同步...");
            return;
        }
        if (orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_FINISH){
            onNext(String.format("订单未完成(%d)，不需要同步...", orderEntity.getStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            onError("会话已失效，暂停同步POS订单数据。");
            return;
        }

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onError("网络未连接，暂停同步POS订单数据。");
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
//                        orderEntity.setUpdatedDate(new Date());
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        onNext(String.format("上传收银订单数据完成。%s",
                                SharedPrefesManagerUltimate.getPosOrderLastUpdate()
                        ));
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        onNext(String.format("上传收银订单失败: %s", errMsg));
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        JSONArray orders = new JSONArray();
        orders.add(generateOrderJson(orderEntity));
        PosOrderApi.batchInOrders(orders.toJSONString(), responseCallback);
    }

    /**
     * 提交遗漏的订单
     * */
    private void uploadMissingOrders(){
        String sqlWhere = String.format("sellerId = '%d' " +
                        "and status = '%d' and isActive = '%d' and syncStatus = '%d'",
                MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE,
                PosOrderEntity.SYNC_STATUS_NONE);
        List<PosOrderEntity> orderEntities = PosOrderService.get()
                .queryAllAsc(sqlWhere, null);
        if (orderEntities != null && orderEntities.size() > 0){
            PosOrderEntity orderEntity = orderEntities.get(0);
            stepUploadPosOrder(orderEntity);
        }
        else{
            onNext(String.format("上传收银订单数据完成。%s",
                    SharedPrefesManagerUltimate.getPosOrderLastUpdate()
            ));
        }
    }

}
