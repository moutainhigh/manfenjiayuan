package com.mfh.framework.rxapi.func;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.http.ApiException;

import rx.functions.Func1;

/**
 * Created by bingshanguxue on 06/01/2017.
 */

public class MQueryResponseFunc<T> implements Func1<MResponse<MRspQuery<T>>, MRspQuery<T>> {
    @Override
    public MRspQuery<T> call(MResponse<MRspQuery<T>> mRspQueryMResponse) {
        if (mRspQueryMResponse == null || mRspQueryMResponse.getCode() != 0) {
            throw new ApiException(mRspQueryMResponse.getMsg(), mRspQueryMResponse.getCode());
        } else {
            MRspQuery<T> rspQuery = mRspQueryMResponse.getData();
            if (rspQuery != null){
                ZLogger.d(String.format("返回查询结果：total=%d", rspQuery.getTotal()));
            }
            else{
                ZLogger.d("返回查询结果为空");
            }

            return mRspQueryMResponse.getData();
        }
    }
}
