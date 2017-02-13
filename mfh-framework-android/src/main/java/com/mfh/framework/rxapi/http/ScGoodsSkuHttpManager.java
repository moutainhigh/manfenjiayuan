package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.scGoodsSku.PosGoods;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 26/01/2017.
 */

public class ScGoodsSkuHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScGoodsSkuHttpManager INSTANCE = new ScGoodsSkuHttpManager();
    }

    //获取单例
    public static ScGoodsSkuHttpManager getInstance() {
        return ScGoodsSkuHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScGoodsSkuService{
        @GET("scGoodsSku/findStoreWithChainSku")
        Observable<MResponse<MRspQuery<ScGoodsSku>>> findStoreWithChainSku(@QueryMap Map<String, String> options);
        @GET("scGoodsSku/downLoadPosProduct")
        Observable<MResponse<MRspQuery<PosGoods>>> downLoadPosProduct(@QueryMap Map<String, String> options);
        /**
         * 查询指定网点可同步sku总数<br>
         * {"code":"0","msg":"查询成功!","version":"1","data":{"val":"701"}}
         */
        @GET("scGoodsSku/countNetSyncAbleSkuNum")
        Observable<MResponse<MValue<String>>> countNetSyncAbleSkuNum(@QueryMap Map<String, String> options);
        @GET("scGoodsSku/getByBarcode")
        Observable<MResponse<ScGoodsSku>> getByBarcode(@QueryMap Map<String, String> options);
        @GET("scGoodsSku/storeIn")
        Observable<MResponse<String>> storeIn(@QueryMap Map<String, String> options);
    }

    public void findStoreWithChainSku(Map<String, String> options, MQuerySubscriber<ScGoodsSku> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.findStoreWithChainSku(options)
                .map(new MResponseFunc<MRspQuery<ScGoodsSku>>());
        toSubscribe(observable, subscriber);
    }

    public void downLoadPosProduct(Map<String, String> options, MQuerySubscriber<PosGoods> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.downLoadPosProduct(options)
                .map(new MQueryResponseFunc<PosGoods>());
        toSubscribe(observable, subscriber);
    }

    public void countNetSyncAbleSkuNum(Map<String, String> options, MValueSubscriber<String> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.countNetSyncAbleSkuNum(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void getByBarcode(Map<String, String> options, Subscriber<ScGoodsSku> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.getByBarcode(options)
                .map(new MResponseFunc<ScGoodsSku>());
        toSubscribe(observable, subscriber);
    }
    public void storeIn(Map<String, String> options, Subscriber<String> subscriber) {
        ScGoodsSkuService mfhApi = RxHttpManager.createService(ScGoodsSkuService.class);
        Observable observable = mfhApi.storeIn(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
}
