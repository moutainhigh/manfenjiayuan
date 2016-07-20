package com.manfenjiayuan.pda_supermarket.utils;


import android.os.Bundle;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.database.entity.ShelveEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.ShelveService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * 货架数据同步
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ShelveSyncManager {
    private static final int MAX_SYNC_PAGESIZE = 10;
    private boolean bSyncInProgress = false;//是否正在同步

    private static ShelveSyncManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static ShelveSyncManager get() {
        if (instance == null) {
            synchronized (ShelveSyncManager.class) {
                if (instance == null) {
                    instance = new ShelveSyncManager();
                }
            }
        }
        return instance;
    }

    /**
     * 上传货架绑定纪录
     */
    public synchronized void sync() {
        if (bSyncInProgress) {
            uploadProcess("OrderSync--正在同步数据...");
            return;
        }

        batchUploadData();
    }

    /**
     * 上传结束
     */
    private void uploadProcess(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = true;
        EventBus.getDefault().post(new ShelveSyncManagerEvent(ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS));
    }

    /**
     * 上传结束
     */
    private void uploadFinished(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new ShelveSyncManagerEvent(ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED));
    }

    /**
     * 上传结束失败
     */
    private void uploadFailed(String msg) {
        ZLogger.df(msg);
        bSyncInProgress = false;
        EventBus.getDefault().post(new ShelveSyncManagerEvent(ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED));
    }

    /**
     * 批量上传货架绑定纪录<br>
     * 根据上一次同步游标同步订单数据
     */
    private void batchUploadData() {
        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("OrderSync--会话已失效，暂停同步货架数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            uploadFailed("OrderSync--网络未连接，暂停同步货架数据。");
            return;
        }

        uploadProcess("OrderSync--检查是否有数据需要上传");

        List<ShelveEntity> entityList = ShelveService.get()
                .queryAllBy(String.format("syncStatus < '%d'", L2CSyncStatus.SYNC_STATUS_FINISHED),
                        new PageInfo(1, MAX_SYNC_PAGESIZE));
        if (entityList == null || entityList.size() < 1) {
            uploadFinished("OrderSync--没有货架数据需要上传");
            return;
        }

        //每次只上传一条纪录
        stepUploadData(entityList.get(0));
    }

    /**
     * 单条上传货架绑定纪录<br>
     * 订单结束时立刻同步
     */
    public void stepUploadData(final ShelveEntity shelveEntity) {
        if (shelveEntity == null){
            uploadFailed("OrderSync--订单无效，不需要同步...");
            return;
        }
        if (L2CSyncStatus.SYNC_STATUS_FINISHED.compareTo(shelveEntity.getSyncStatus()) <= 0){
            uploadFailed(String.format("OrderSync--订单状态(%d)无效，不需要同步...", shelveEntity.getSyncStatus()));
            return;
        }

        if (!MfhLoginService.get().haveLogined()) {
            uploadFailed("OrderSync--会话已失效，暂停同步货架绑定纪录数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            uploadFailed("OrderSync--网络未连接，暂停同步货架绑定纪录数据。");
            return;
        }

        uploadProcess(String.format("OrderSync--准备上传货架绑定纪录(%d/%s)", shelveEntity.getId(),
                shelveEntity.getBarcode()));

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();

                        //出库成功:1-556637
                        ZLogger.d("上传绑定记录成功:" + retStr);

                        shelveEntity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_FINISHED);
                        shelveEntity.setUpdatedDate(new Date());
                        ShelveService.get().saveOrUpdate(shelveEntity);

                        //继续提交
                        batchUploadData();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("DataSync--上传绑定记录失败: " + errMsg);
                        uploadFailed(errMsg);
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        StockApiImpl.bindRackNo(shelveEntity.getBarcode(), shelveEntity.getRackNo(), responseCallback);
    }

    public class ShelveSyncManagerEvent {
        public static final int EVENT_ID_SYNC_DATA_START    = 0X03;//同步数据开始
        public static final int EVENT_ID_SYNC_DATA_PROCESS  = 0X04;//同步数据处理中
        public static final int EVENT_ID_SYNC_DATA_FINISHED = 0X05;//同步数据结束
        public static final int EVENT_ID_SYNC_DATA_FAILED   = 0X06;//同步数据失败

        private int eventId;
        private Bundle args;//参数

        public ShelveSyncManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public ShelveSyncManagerEvent(int eventId, Bundle args) {
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
