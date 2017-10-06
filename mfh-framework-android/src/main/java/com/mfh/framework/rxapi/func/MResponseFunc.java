package com.mfh.framework.rxapi.func;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.ApiException;
import com.mfh.framework.rxapi.http.ErrorCode;

import rx.functions.Func1;

/**
 * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
 *
 * @param <T> Subscriber真正需要的数据类型，也就是Data部分的数据类型
 *
 * Created by bingshanguxue on 06/01/2017.
 */

public class MResponseFunc<T> implements Func1<MResponse<T>, T> {
    @Override
    public T call(MResponse<T> tmResponse) {
        if (tmResponse == null) {
            throw new ApiException(ApiException.NULL);
        } else {
            ZLogger.d(tmResponse.getCode() + " > " + tmResponse.getMsg());
            switch (tmResponse.getCode()) {
                case ErrorCode.SUCCESS:
                    return tmResponse.getData();
                case ErrorCode.LOGIN_ERROR:
                default:
                    throw new ApiException(tmResponse.getMsg(), tmResponse.getCode());

            }
        }
    }
}
