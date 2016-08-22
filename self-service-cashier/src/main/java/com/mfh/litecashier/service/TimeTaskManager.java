package com.mfh.litecashier.service;

import android.os.Handler;
import android.os.Message;

import com.mfh.framework.BizConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bingshanguxue on 8/22/16.
 */
public class TimeTaskManager {
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = 60 * 60 * 1000;

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


    private final Timer syncPosOrderTimer = new Timer();
    private TimerTask syncPosOrderTask = new TimerTask() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            UploadSyncManager.getInstance().sync(UploadSyncManager.SyncStep.CASHIER_ORDER);

            // 要做的事情
            super.handleMessage(msg);
        }
    };

    /**定时同步POS订单*/
    public void start(){
        if (BizConfig.RELEASE){
            syncPosOrderTimer.schedule(syncPosOrderTask, 10 * MINUTE, 10 * 1000);
        }
        else{
            syncPosOrderTimer.schedule(syncPosOrderTask, 10 * SECOND, 10 * 1000);
        }
    }

    public void cancel(){
        syncPosOrderTimer.cancel();
    }
}
