package com.mfh.framework.rxapi.func;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.http.ApiException;

import rx.functions.Func1;

/**
 * Created by bingshanguxue on 06/01/2017.
 */

public class MValueResponseFunc<T> implements Func1<MResponse<MValue<T>>, MValue<T>> {
    @Override
    public MValue<T> call(MResponse<MValue<T>> mRspQueryMResponse) {
        if (mRspQueryMResponse == null || mRspQueryMResponse.getCode() != 0) {
            throw new ApiException(mRspQueryMResponse.getMsg(), mRspQueryMResponse.getCode());
        } else {
            return mRspQueryMResponse.getData();
        }
    }
}
