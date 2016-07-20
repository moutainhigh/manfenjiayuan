package com.mfh.framework.network;

/**
 * 网络事件回调接口
 */
public interface NetStateListener {
    /**
     * 接通时执行
     */
    void onConnected();

    /**
     * 断开时执行
     */
    void onDisConnected();
}