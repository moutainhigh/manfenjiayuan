package com.mfh.framework.rxapi.http;


import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.CategoryQueryInfo;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 8/29/16.
 */

public class ScCategoryInfoHttpManager  extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScCategoryInfoHttpManager INSTANCE = new ScCategoryInfoHttpManager();
    }

    //获取单例
    public static ScCategoryInfoHttpManager getInstance() {
        return ScCategoryInfoHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScCategoryInfoService{
        @GET("scCategoryInfo/comnQuery")
        Observable<MResponse<CategoryQueryInfo>> comnQuery(@QueryMap Map<String, String> options);
        @GET("scCategoryInfo/getTopFrontId")
        Observable<MResponse<CategoryInfo>> getTopFrontId(@QueryMap Map<String, String> options);
        /**加载类目*/
        @GET("scCategoryInfo/getCodeValue")
        Observable<MResponse<List<CategoryInfo>>> getCodeValue(@QueryMap Map<String, String> options);
        @GET("scCategoryInfo/create")
        Observable<MResponse<MValue<String>>> create(@Query("JSESSIONID") String JSESSIONID,
                                                     @Query("jsonStr") String jsonStr);
        /**pos导入商品到前台类目时，加载平台维护的POS前台类目*/
        @GET("scCategoryInfo/list")
        Observable<MResponse<MRspQuery<CategoryInfo>>> list(@QueryMap Map<String, String> options);
    }

    public void comnQuery(Map<String, String> options, Subscriber<CategoryQueryInfo> subscriber) {
        ScCategoryInfoService mfhApi = RxHttpManager.createService(ScCategoryInfoService.class);
        Observable observable = mfhApi.comnQuery(options)
                .map(new MResponseFunc<CategoryQueryInfo>());
        toSubscribe(observable, subscriber);
    }
    public void getTopFrontId(Map<String, String> options, Subscriber<CategoryInfo> subscriber) {
        ScCategoryInfoService mfhApi = RxHttpManager.createService(ScCategoryInfoService.class);
        Observable observable = mfhApi.getTopFrontId(options)
                .map(new MResponseFunc<CategoryInfo>());
        toSubscribe(observable, subscriber);
    }
    public void getCodeValue(Map<String, String> options, Subscriber<List<CategoryInfo>> subscriber) {
        ScCategoryInfoService mfhApi = RxHttpManager.createService(ScCategoryInfoService.class);
        Observable observable = mfhApi.getCodeValue(options)
                .map(new MResponseFunc<List<CategoryInfo>>());
        toSubscribe(observable, subscriber);
    }
    public void create(String JSESSIONID, JSONObject jsonStr, MValueSubscriber<String> subscriber) {
        ScCategoryInfoService mfhApi = RxHttpManager.createService(ScCategoryInfoService.class);
        Observable observable = mfhApi.create(JSESSIONID, jsonStr.toJSONString())
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void list(Map<String, String> options, MQuerySubscriber<CategoryInfo> subscriber) {
        ScCategoryInfoService mfhApi = RxHttpManager.createService(ScCategoryInfoService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<CategoryInfo>());
        toSubscribe(observable, subscriber);
    }

}
