package com.jabra.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jabra.listener.NetworkListener;
import com.mfh.framework.anlaysis.logger.ZLogger;

public class NetworkReceiver
        extends BroadcastReceiver {

    private NetworkListener mNetworkListener;

    public void onReceive(Context context, Intent intent) {
        NetworkInfo localNetworkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (this.mNetworkListener != null) {
            if (localNetworkInfo == null) {
                ZLogger.w("网络不可用");
                this.mNetworkListener.onNetworkDisable();
            } else {
                ZLogger.v("网络已连接");
                this.mNetworkListener.onNetworkUsable();

                // TODO: 06/09/2017
//    WebManager.getInstance(paramContext).uploadFaileQueue();
            }
        }
    }


    public void register(Context context, NetworkListener networkListener) {
        context.registerReceiver(this, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        this.mNetworkListener = networkListener;
    }

    public void unregister(Context context) {
        try {
            context.unregisterReceiver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
