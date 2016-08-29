package com.mfh.framework.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.IService;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 网络状态服务监听类
 * Created by bingshanguxue on 14-6-12.
 */
public class NetStateService implements IService {
    private boolean connected = true;

    private static NetStateService instance = null;

    public static NetStateService getInstance() {
        if (instance == null) {
            synchronized (NetStateService.class) {
                if (instance == null) {
                    instance = new NetStateService();
                }
            }
        }
        return instance;
    }

    private Set<NetStateListener> listeners = new HashSet<>();

    /**
     * 增加一个监听事件
     *
     * @param listener
     * @param connectivityType 对哪种网络感兴趣，备用
     */
    public synchronized void addListener(NetStateListener listener, int... connectivityType) {
        if (listeners == null){
            listeners = new HashSet<>();
        }
        listeners.add(listener);
    }

    /**
     * 移除一个监听事件
     *
     * @param listener
     */
    public synchronized void removeListener(NetStateListener listener) {
        if (listeners != null){
            listeners.remove(listener);
        }
    }

    /**
     * 注册网络状态事件接收器
     */
    public void register(Context context) {
        if (context == null || networkStateReceiver == null){
            ZLogger.df("注册网络状态事件接收器失败");
            return;
        }

        ZLogger.df("注册网络状态事件接收器...");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkStateReceiver, filter);
    }

    /**
     * 反注册网络状态事件接收器
     */
    public void unregister(Context context) {
        if (context == null || networkStateReceiver == null){
            ZLogger.d("unregister failed");
            return;
        }

        context.unregisterReceiver(networkStateReceiver);
    }

    BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo workInfo = connManager.getActiveNetworkInfo();
            NetworkInfo.State state = null;
            if (workInfo != null)
                state = workInfo.getState();
            //ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI

            if (state != null && NetworkInfo.State.CONNECTED == state) {
                connected = true;
                synchronized (listeners) {
                    Iterator<NetStateListener> iter = listeners.iterator();
                    while (iter.hasNext()) {
                        iter.next().onConnected();
                    }
                }
            } else {
                connected = true;
                synchronized (listeners) {
                    Iterator<NetStateListener> iter = listeners.iterator();
                    while (iter.hasNext()) {
                        iter.next().onDisConnected();
                    }
                }
            }
        }
    };
}
