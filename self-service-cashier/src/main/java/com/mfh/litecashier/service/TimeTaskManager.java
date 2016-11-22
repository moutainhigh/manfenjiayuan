package com.mfh.litecashier.service;

import android.os.Handler;
import android.os.Message;

import com.mfh.framework.anlaysis.logger.ZLogger;

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
     */
    private static Timer syncPosOrderTimer = new Timer();

    /**
     * 同步商品
     */
    private static Timer syncGoodsTimer = new Timer();


    static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG_WHAT_SYNC_POSORDER: {
                    ZLogger.df("定时任务激活：上传收银订单");
                    UploadSyncManager.getInstance().sync(UploadSyncManager.SyncStep.CASHIER_ORDER);
                }
                break;
                case MSG_WHAT_SYNC_POSGOODS: {
                    ZLogger.df("定时任务激活：同步商品库");
                    DataDownloadManager.get().sync(DataDownloadManager.POSPRODUCTS | DataDownloadManager.POSPRODUCTS_SKU);
                }
                break;
            }

            // 要做的事情
            super.handleMessage(msg);
        }
    };

    /**
     * 定时同步POS订单
     */
    public void start() {
        cancel();
        ZLogger.d("定时任务开启...");
        if (syncPosOrderTimer == null) {
            syncPosOrderTimer = new Timer();
        }
        if (syncGoodsTimer == null) {
            syncGoodsTimer = new Timer();
        }

        syncPosOrderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MSG_WHAT_SYNC_POSORDER;
                handler.sendMessage(message);
            }
        }, 10 * SECOND, 10 * MINUTE);
        syncGoodsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = MSG_WHAT_SYNC_POSGOODS;
                handler.sendMessage(message);
            }
        }, 10 * SECOND, 6 * HOUR);
    }

    public void cancel() {
        ZLogger.d("取消定时任务...");
        if (syncPosOrderTimer != null) {
            syncPosOrderTimer.cancel();
        }
        syncPosOrderTimer = null;
        if (syncGoodsTimer != null) {
            syncGoodsTimer.cancel();
        }
        syncGoodsTimer = null;
    }
}
