package com.mfh.framework.rxapi.http;


import com.mfh.framework.rxapi.entity.MResponse;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by bingshanguxue on 8/29/16.
 */

public interface RxAnalysisService {
    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     * /analysisAccDate/commintCashAndTrigDateEnd?date=2016-02-18&outTradeNo=7_1000228_1452600164232
     */
    @FormUrlEncoded
    @POST("analysisAccDate/commintCashAndTrigDateEnd")
    Observable<MResponse<String>> commintCashAndTrigDateEnd(@Field("outTradeNo") String outTradeNo);
}
