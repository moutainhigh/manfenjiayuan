package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.anon.sc.productPrice.PubSkus;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class AnonScHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final AnonScHttpManager INSTANCE = new AnonScHttpManager();
    }

    //获取单例
    public static AnonScHttpManager getInstance() {
        return AnonScHttpManager.SingletonHolder.INSTANCE;
    }

    private interface AnonScService{
        /**
         * /anon/sc/productPrice/findPubSkusByFrontCatalog?frontCataLogId=&priceMask=0
         * 根据前台类目查找有效的商品列表(前台类目可能又包含后台类目),返回的id就是proSkuId，另外还有productId
         * */
        @GET("anon/sc/productPrice/findPubSkusByFrontCatalog")
        Observable<MResponse<MRspQuery<PubSkus>>> findPubSkusByFrontCatalog(@QueryMap Map<String, String> options);


        /**
         * 查询商品：
         * anon/sc/productPrice/getById?id=
         * id是proSkuId
         * */
        @GET("anon/sc/productPrice/getById")
        Observable<MResponse<PubSkus>> getById(@QueryMap Map<String, String> options);
    }

    public void findPubSkusByFrontCatalog(Map<String, String> options,
                                          MQuerySubscriber<PubSkus> subscriber) {
        AnonScService mfhApi = RxHttpManager.createService(AnonScService.class);
        Observable observable = mfhApi.findPubSkusByFrontCatalog(options)
                .map(new MQueryResponseFunc<PubSkus>());
        toSubscribe(observable, subscriber);
    }

    public void getById(Map<String, String> options, Subscriber<PubSkus> subscriber) {
        AnonScService mfhApi = RxHttpManager.createService(AnonScService.class);
        Observable observable = mfhApi.getById(options)
                .map(new MResponseFunc<PubSkus>());
        toSubscribe(observable, subscriber);
    }



}
