package com.mfh.framework.rxapi.subscriber;

import com.mfh.framework.rxapi.entity.MValue;

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
    }

    @Override
    public void onNext(MValue<T> mValue) {
        if (mValue != null){
            onValue(mValue.getVal());
        }
        else{
            onValue(null);
        }
    }

    public void onValue(T data){

    }


}
