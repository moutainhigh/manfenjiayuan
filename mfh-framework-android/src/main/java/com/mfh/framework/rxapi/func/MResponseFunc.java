package com.mfh.framework.rxapi.func;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.ApiException;

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
        if (tmResponse == null || tmResponse.getCode() != 0) {
            throw new ApiException(tmResponse.getMsg(), tmResponse.getCode());
        } else {
            ZLogger.d("返回真正需要的数据");
            return tmResponse.getData();
        }
    }
}
