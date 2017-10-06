package com.mfh.framework.rxapi.httpmgr;


import com.mfh.framework.api.anon.sc.ProductCatalog;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 8/29/16.
 */

public class ProductCatalogManager extends BaseHttpManager {

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ProductCatalogManager INSTANCE = new ProductCatalogManager();
    }

    //获取单例
    public static ProductCatalogManager getInstance() {
        return ProductCatalogManager.SingletonHolder.INSTANCE;
    }

    private interface ProductCatalogService{
        @GET("anon/sc/productCatalog/downLoadProductCatalog")
        Observable<MResponse<MRspQuery<ProductCatalog>>> downLoadProductCatalog(@QueryMap Map<String, String> options);
        @GET("anon/sc/productCatalog/countProductCatalogSyncAbleNum")
        Observable<MResponse<MValue<String>>> countProductCatalogSyncAbleNum(@QueryMap Map<String, String> options);
        /**
         * 把几个商品添加到指定前台类目中：  /anon/sc/productCatalog/addToCatalog?groupIds=3397&productIds=20551&catalogType=1
         * 其中groupIds为建好的前台类目，productIds为商品的spuId（不是skuId）
         * spuId就是productId
         */
        @GET("anon/sc/productCatalog/addToCatalog")
        Observable<MResponse<String>> addToCatalog(@QueryMap Map<String, String> options);
        @GET("anon/sc/productCatalog/delete")
        Observable<MResponse<String>> delete(@QueryMap Map<String, String> options);
    }

    public void downLoadProductCatalog(Map<String, String> options, MQuerySubscriber<ProductCatalog> subscriber) {
        ProductCatalogService mfhApi = RxHttpManager.createService(ProductCatalogService.class);
        Observable observable = mfhApi.downLoadProductCatalog(options)
                .map(new MQueryResponseFunc<ProductCatalog>());
        toSubscribe(observable, subscriber);
    }

    public void countProductCatalogSyncAbleNum(Map<String, String> options, MValueSubscriber<String> subscriber) {
        ProductCatalogService mfhApi = RxHttpManager.createService(ProductCatalogService.class);
        Observable observable = mfhApi.countProductCatalogSyncAbleNum(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void addToCatalog(Map<String, String> options, Subscriber<String> subscriber) {
        ProductCatalogService mfhApi = RxHttpManager.createService(ProductCatalogService.class);
        Observable observable = mfhApi.addToCatalog(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void delete(Map<String, String> options, Subscriber<String> subscriber) {
        ProductCatalogService mfhApi = RxHttpManager.createService(ProductCatalogService.class);
        Observable observable = mfhApi.delete(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

}
