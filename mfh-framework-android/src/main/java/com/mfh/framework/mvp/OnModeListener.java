package com.mfh.framework.mvp;

/**
 *
 * Created by bingshanguxue on 16/3/17.
 */
public interface OnModeListener<M> {
    void onProcess();
    void onSuccess(M data);
    void onError(String errorMsg);
}
