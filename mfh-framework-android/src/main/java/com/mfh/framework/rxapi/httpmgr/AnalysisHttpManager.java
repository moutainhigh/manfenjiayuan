package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * 日结&交接班
 * Created by bingshanguxue on 25/01/2017.
 */

public class AnalysisHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final AnalysisHttpManager INSTANCE = new AnalysisHttpManager();
    }

    //获取单例
    public static AnalysisHttpManager getInstance() {
        return AnalysisHttpManager.SingletonHolder.INSTANCE;
    }

    private interface AnalysisService {
        /**
         * 启动日结统计
         * /bizServer/autoDateEnd?date=2016-02-02
         */
        @GET("bizServer/autoDateEnd")
        Observable<MResponse<String>> autoDateEnd(@QueryMap Map<String, String> options);
        /**
         * 日结经营分析查询
         * 经营分析：/analysisAggDate/list?aggDate=2016-02-17&officeId=131295
         */
        @GET("analysisAggDate/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<AggItem>>>> analysisAggDateList(@QueryMap Map<String, String> options);
        /**
         * 日结流水分析查询
         * 流水分析：/analysisAccDate/list?officeId=132079&aggDate=2016-02-18&wrapper=true
         * bizDomain=0代表查询的经营性流水； bizDomain=1代表查询的充值部分的流水
         */
        @GET("analysisAccDate/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<AccItem>>>> analysisAccDateList(@QueryMap Map<String, String> options);
        /**
         * 启动交接班统计
         * /bizServer/autoShiftAnalysis?shiftId=1&startTime=2016-02-02 07:00:00&endTime=2016-02-02 19:00:00
         */
        @GET("bizServer/autoShiftAnalysis")
        Observable<MResponse<String>> autoShiftAnalysis(@QueryMap Map<String, String> options);
        /**
         * 交接班流水分析查询
         * /analysisAggShift/list?aggDate=2016-02-02&shiftId=1&createdBy=134475，finishDay参数改成aggDate
         */
        @GET("analysisAggShift/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<AggItem>>>> analysisAggShiftList(@QueryMap Map<String, String> options);
        /**
         * 交接班经营分析查询
         * /accAnalysisAggShift/list?aggDate=2016-02-02&createdBy=134475&shiftId=1
         */
        @GET("accAnalysisAggShift/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<AccItem>>>> accAnalysisAggShiftList(@QueryMap Map<String, String> options);

    }

    public void autoDateEnd(Map<String, String> options, Subscriber<String> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.autoDateEnd(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void analysisAggDateList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AggItem>> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.analysisAggDateList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AggItem>>());
        toSubscribe(observable, subscriber);
    }

    public void analysisAccDateList(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<AccItem>> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.analysisAccDateList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AccItem>>());
        toSubscribe(observable, subscriber);
    }

    public void autoShiftAnalysis(Map<String, String> options, Subscriber<String> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.autoShiftAnalysis(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void analysisAggShiftList(Map<String, String> options,
                                     MQuerySubscriber<MEntityWrapper<AggItem>> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.analysisAggShiftList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AggItem>>());
        toSubscribe(observable, subscriber);
    }

    public void accAnalysisAggShiftList(Map<String, String> options,
                                        MQuerySubscriber<MEntityWrapper<AccItem>> subscriber) {
        AnalysisService mfhApi = RxHttpManager.createService(AnalysisService.class);
        Observable observable = mfhApi.accAnalysisAggShiftList(options)
                .map(new MQueryResponseFunc<MEntityWrapper<AccItem>>());
        toSubscribe(observable, subscriber);
    }



}
