package com.mfh.framework.login.logic;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface Callback {
    void onSuccess();
    void onProgress(int progress, String status);
    void onError(int code, String message);
}
