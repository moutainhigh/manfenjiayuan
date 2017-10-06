package com.mfh.framework.rxapi.subscriber;

import android.content.Intent;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.http.ExceptionHandle;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 26/12/2016.
 */

public class MValueSubscriber<T> extends Subscriber<MValue<T>> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        ZLogger.e("MValueSubscriber.throwable =" + e.toString());
        ZLogger.e("MValueSubscriber.throwable =" + e.getMessage());

        ExceptionHandle.ResponeThrowable throwable;
        if (e instanceof Exception) {
            //访问获得对应的Exception
//            onError(ExceptionHandle.handleException(e));
            throwable = ExceptionHandle.handleException(e);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        } else {
            //将Throwable 和 未知错误的status code返回
            throwable = new ExceptionHandle.ResponeThrowable(e, ExceptionHandle.ERROR.UNKNOWN);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        }
    }

    @Override
    public void onNext(MValue<T> mValue) {
        if (mValue != null) {
            onValue(mValue.getVal());
        } else {
            onValue(null);
        }
    }

    public void onValue(T data) {

    }


}
