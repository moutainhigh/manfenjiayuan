package com.mfh.litecashier.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.ui.activity.UnauthorizedActivity;

/**
 * Created by bingshanguxue on 24/09/2017.
 */

public class MainService extends Service {
    public static final String ACTION_UNAUTHORIZED = "com.mfh.litecashier.service.ACTION_UNAUTHORIZED";

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UNAUTHORIZED);
        registerReceiver(authReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ZLogger.d("onStartCommand");
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (authReceiver != null) {
            unregisterReceiver(authReceiver);
        }
    }


    BroadcastReceiver authReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ZLogger.d(action);

            if (ACTION_UNAUTHORIZED.equals(action)) {
                try {
                    Intent mIntent = new Intent(context, UnauthorizedActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mIntent.putExtra(UnauthorizedActivity.EXTRA_KEY_LOGIN_TYPE, UnauthorizedActivity.LOGIN_TYPE_CARD);
                    startActivity(mIntent);
                } catch (Exception e) {
                    ZLogger.ef(e.toString());
                }
            }
        }
    };
}
