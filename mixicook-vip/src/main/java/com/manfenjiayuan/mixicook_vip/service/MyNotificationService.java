package com.manfenjiayuan.mixicook_vip.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.manfenjiayuan.mixicook_vip.BuildConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;

public class MyNotificationService extends NotificationListenerService {

    private static final String TAG = "QHBNotificationService";

    private static MyNotificationService service;

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onListenerConnected();
        }
    }

    private ServiceConfig getConfig() {
        return ServiceConfig.getConfig(this);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        ZLogger.i("onNotificationPosted: " + sbn.toString());

//        if(!getConfig().isAgreement()) {
//            return;
//        }
//        if(!getConfig().isEnableNotificationService()) {
//            return;
//        }
//        MyAccesibilityService.handeNotificationPosted(new IStatusBarNotification() {
//            @Override
//            public String getPackageName() {
//                return sbn.getPackageName();
//            }
//
//            @Override
//            public Notification getNotification() {
//                return sbn.getNotification();
//            }
//        });
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onNotificationRemoved(sbn);
        }
        ZLogger.i("onNotificationRemoved");
    }

    @Override
    public void onListenerConnected() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onListenerConnected();
        }

        ZLogger.i("onListenerConnected");
        service = this;
        //发送广播，已经连接上了
//        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
//        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZLogger.i("onDestroy");
        service = null;
        //发送广播，已经连接上了
//        Intent intent = new Intent(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
//        sendBroadcast(intent);
    }

    /** 是否启动通知栏监听*/
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        return true;
    }
}