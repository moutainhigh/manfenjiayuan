package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvSkuStoreHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvSkuStoreHttpManager INSTANCE = new InvSkuStoreHttpManager();
    }

    //获取单例
    public static InvSkuStoreHttpManager getInstance() {
        return InvSkuStoreHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvSkuStoreService{
        @GET("invSkuStore/importFromCenterSkus")
        Observable<MResponse<String>> importFromCenterSkus(@QueryMap Map<String, String> options);
        @GET("invSkuStore/update")
        Observable<MResponse<String>> update(@QueryMap Map<String, String> options);
        @GET("invSkuStore/updateStatus")
        Observable<MResponse<String>> updateStatus(@QueryMap Map<String, String> options);
    }

    public void importFromCenterSkus(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.importFromCenterSkus(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void update(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.update(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void updateStatus(Map<String, String> options, Subscriber<String> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.updateStatus(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

}
