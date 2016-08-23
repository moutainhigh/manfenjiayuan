package com.mfh.litecashier.service;

import android.os.Handler;
import android.os.Message;

import com.mfh.framework.BizConfig;
import com.mfh.framework.core.logger.ZLogger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bingshanguxue on 8/22/16.
 */
public class TimeTaskManager {
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * 60 * 1000;

    private static final int MSG_WHAT_SYNC_POSORDER = 1;
    private static final int MSG_WHAT_SYNC_POSGOODS = 2;


    private static TimeTaskManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static TimeTaskManager getInstance() {
        if (instance == null) {
            synchronized (TimeTaskManager.class) {
                if (instance == null) {
                    instance = new TimeTaskManager();
                }
            }
        }
        return instance;
    }


    /**
     * 同步订单
     * */
    private final Timer syncPosOrderTimer = new Timer();
    private TimerTask syncPosOrderTask = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = MSG_WHAT_SYNC_POSORDER;
            handler.sendMessage(message);
        }
    };

    /**
     * 同步商品
     * */
    private final Timer syncGoodsTimer = new Timer();
    private TimerTask syncGoodsTask = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = MSG_WHAT_SYNC_POSGOODS;
            handler.sendMessage(message);
        }
    };


    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case MSG_WHAT_SYNC_POSORDER:{
                    UploadSyncManager.getInstance().sync(UploadSyncManager.SyncStep.CASHIER_ORDER);
                }
                break;
                case MSG_WHAT_SYNC_POSGOODS:{
                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
                break;
            }

            // 要做的事情
            super.handleMessage(msg);
        }
    };

    /**定时同步POS订单*/
    public void start(){
        ZLogger.d("定时任务开启...");
        if (BizConfig.RELEASE){
            syncPosOrderTimer.schedule(syncPosOrderTask, 10 * SECOND, 10 * MINUTE);
            syncGoodsTimer.schedule(syncGoodsTask, 10 * SECOND, 6 * HOUR);
        }
        else{
            syncPosOrderTimer.schedule(syncPosOrderTask, 10 * SECOND, 10 * SECOND);
            syncGoodsTimer.schedule(syncGoodsTask, 10 * SECOND, 1 * MINUTE);
        }
    }

    public void cancel(){
        ZLogger.d("取消定时任务...");
        syncPosOrderTimer.cancel();
    }
}
