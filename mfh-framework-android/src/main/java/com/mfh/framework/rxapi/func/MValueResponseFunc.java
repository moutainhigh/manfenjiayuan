package com.mfh.framework.rxapi.func;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.http.ApiException;
import com.mfh.framework.rxapi.http.ErrorCode;

import rx.functions.Func1;

/**
 * Created by bingshanguxue on 06/01/2017.
 */

public class MValueResponseFunc<T> implements Func1<MResponse<MValue<T>>, MValue<T>> {
    @Override
    public MValue<T> call(MResponse<MValue<T>> mRspQueryMResponse) {
        if (mRspQueryMResponse == null) {
            throw new ApiException(ApiException.NULL);
        } else {
            ZLogger.d(mRspQueryMResponse.getCode() + " > " + mRspQueryMResponse.getMsg());

            if (!ErrorCode.SUCCESS.equals(mRspQueryMResponse.getCode())) {
                throw new ApiException(mRspQueryMResponse.getMsg(), mRspQueryMResponse.getCode());
            } else {
                return mRspQueryMResponse.getData();
            }
        }
    }
}
