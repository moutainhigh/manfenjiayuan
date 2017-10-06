package com.mfh.framework.rxapi.subscriber;

import android.content.Intent;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.http.ExceptionHandle;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 26/12/2016.
 */

public abstract class MSubscriber<T> extends Subscriber<T> {

    @Override
    public void onStart() {
        super.onStart();

//        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
//            DialogUtil.showHint(R.string.toast_network_error);
//
//            // 一定好主动调用下面这一句,取消本次Subscriber订阅
//            if (!isUnsubscribed()) {
//                unsubscribe();
//            }
//            return;
//        }
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        ZLogger.e("MSubscriber.throwable =" + e.toString());
        ZLogger.e("MSubscriber.throwable =" + e.getMessage());

        ExceptionHandle.ResponeThrowable throwable;
        if (e instanceof Exception) {
            //访问获得对应的Exception
            throwable = ExceptionHandle.handleException(e);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        } else {
            //将Throwable 和 未知错误的status code返回
            throwable = new ExceptionHandle.ResponeThrowable(e,
                    ExceptionHandle.ERROR.UNKNOWN);
            if (throwable.code == ExceptionHandle.ERROR.HTTP_ERROR_RETRY_LOGIN) {
                MfhApplication.getAppContext().sendBroadcast(new Intent("com.mfh.litecashier.service.ACTION_UNAUTHORIZED"));
            }
        }
        onError(throwable);

    }

    public abstract void onError(ExceptionHandle.ResponeThrowable e);

    @Override
    public void onNext(T t) {
    }


}
