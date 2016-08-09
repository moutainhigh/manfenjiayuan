package com.mfh.litecashier.service;


import com.bingshanguxue.cashier.PayStatus;
import com.bingshanguxue.cashier.SyncStatus;
import com.bingshanguxue.cashier.database.entity.PosTopupEntity;
import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.analysis.AnalysisApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.litecashier.CashierApp;

import java.util.List;

/**
 * 上传数据，确保POS机数据能正确的上传到云端
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class UploadSyncManager {
    public interface SyncStep {
        int STANDBY = -1;
        int INCOME_DISTRIBUTION_TOPUP = 1;//清分充值
        int CASH_QUOTA_TOPUP = 2;//现金授权充值
    }

    private boolean bSyncInProgress = false;//是否正在同步
    //当前同步进度
    private int nextStep = SyncStep.STANDBY;

    private static UploadSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
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
            nextStep = SyncStep.INCOME_DISTRIBUTION_TOPUP;
            return;
        }

        processStep(SyncStep.INCOME_DISTRIBUTION_TOPUP, SyncStep.CASH_QUOTA_TOPUP);
    }

    public void sync(int step) {
        if (bSyncInProgress) {
            ZLogger.df("正在同步POS数据...");
            if (nextStep > step){
                nextStep = step;
            }
            return;
        }

        processStep(step, SyncStep.STANDBY);
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

        switch (step) {
            case SyncStep.INCOME_DISTRIBUTION_TOPUP: {
                commintCashAndTrigDateEnd();
            }
            break;
            case SyncStep.CASH_QUOTA_TOPUP: {
                commintCash();
            }
            break;
            default: {
                ZLogger.df("同步POS数据结束");
                bSyncInProgress = false;
//                EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
            }
            break;
        }
    }

    private void networkError() {
        ZLogger.df("网络未连接，暂停同步POS数据。");
        bSyncInProgress = false;
//        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    private void sessionError() {
        ZLogger.df("会话已失效，暂停同步POS数据。");
        bSyncInProgress = false;
//        EventBus.getDefault().post(new DataSyncEvent(DataSyncEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }


    /**
     * 提交营业现金，并触发一次日结操作
     */
    private void commintCashAndTrigDateEnd() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        String sqlWhere = String.format("bizType = '%' and paystatus = '%d' and syncStatus = '%d'",
                BizType.INCOME_DISTRIBUTION, PayStatus.FINISH, SyncStatus.INIT);

        PosTopupEntity topupEntity = null;
        List<PosTopupEntity> entities = PosTopupService.get().queryAllBy(sqlWhere);
        if (entities != null && entities.size() > 0){
            topupEntity = entities.get(0);
        }

        if (topupEntity != null){
            final PosTopupEntity finalTopupEntity = topupEntity;
            NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"操作成功!","version":"1","data":false}
                            //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                            //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                            RspValue<String> retValue = (RspValue<String>) rspData;

                            if (finalTopupEntity != null){
                                finalTopupEntity.setSyncStatus(SyncStatus.SYNCED);
                                PosTopupService.get().saveOrUpdate(finalTopupEntity);
                            }

                            commintCashAndTrigDateEnd();
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
//                        {"code":"1","msg":"未找到支付交易号:2016-08-02","data":null,"version":1}

                            nextStep();
                        }
                    }
                    , String.class
                    , CashierApp.getAppContext()) {
            };

            AnalysisApiImpl.commintCashAndTrigDateEnd(topupEntity.getOutTradeNo(), responseRC);
        }
        else{
            nextStep();
        }
    }

    /**
     * 提交营业现金
     */
    private void commintCash() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            networkError();
            return;
        }

        String sqlWhere = String.format("bizType = '%' and paystatus = '%d' and syncStatus = '%d'",
                BizType.CASH_QUOTA, PayStatus.FINISH, SyncStatus.INIT);

        PosTopupEntity topupEntity = null;
        List<PosTopupEntity> entities = PosTopupService.get().queryAllBy(sqlWhere);
        if (entities != null && entities.size() > 0){
            topupEntity = entities.get(0);
        }

        if (topupEntity != null){
            final PosTopupEntity finalTopupEntity = topupEntity;
            NetCallBack.NetTaskCallBack responseRC = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"操作成功!","version":"1","data":false}
                            //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                            //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                            RspValue<String> retValue = (RspValue<String>) rspData;

                            if (finalTopupEntity != null){
                                finalTopupEntity.setSyncStatus(SyncStatus.SYNCED);
                                PosTopupService.get().saveOrUpdate(finalTopupEntity);
                            }

                            commintCash();
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
//                        {"code":"1","msg":"未找到支付交易号:2016-08-02","data":null,"version":1}

                            nextStep();
                        }
                    }
                    , String.class
                    , CashierApp.getAppContext()) {
            };

            AnalysisApiImpl.commintCash(topupEntity.getOutTradeNo(), responseRC);
        }
        else{
            nextStep();
        }
    }

//    public static class DataSyncEvent {
//        public static final int EVENT_ID_REFRESH_FRONT_CATEGORYINFO         = 0X01;//刷新前台类目树
//        public static final int EVENT_ID_REFRESH_FRONTEND_CATEGORYINFO_FRESH = 0X02;//刷新前台生鲜类目树
//        public static final int EVENT_ID_REFRESH_BACKEND_CATEGORYINFO       = 0X03;//刷新后台类目树
//        public static final int EVENT_ID_REFRESH_BACKEND_CATEGORYINFO_FRESH = 0X04;//刷新后台生鲜类目树
//        public static final int EVENT_ID_SYNC_DATA_PROGRESS = 0X11;//同步进度
//        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X12;//同步结束
//
//        private int eventId;
//
//        public DataSyncEvent(int eventId) {
//            this.eventId = eventId;
//        }
//
//        public int getEventId() {
//            return eventId;
//        }
//    }
}
