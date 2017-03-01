package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.invSendOrder.InvSendOrderItemBrief;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
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
        /**
         * 根据条码查询库存商品,如果库存中没有则从租户档案中自动建立库存。门店和批发都适用
         * /invSkuStore/getByBarcodeMust?barcode=998800000000
         */
        @GET("invSkuStore/getByBarcodeMust")
        Observable<MResponse<InvSkuGoods>> getByBarcodeMust(@QueryMap Map<String, String> options);

        /**
         * 当前登录网点的操作人员，通过指定一个批发商，自动生成配送单
         * /invSkuStore/autoAskSendOrder?chainCompanyId=134651
         */
        @GET("invSkuStore/autoAskSendOrder")
        Observable<MResponse<InvSendOrderItemBrief>> autoAskSendOrder(@QueryMap Map<String, String> options);
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

    public void getByBarcodeMust(Map<String, String> options, Subscriber<InvSkuGoods> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.getByBarcodeMust(options)
                .map(new MResponseFunc<InvSkuGoods>());
        toSubscribe(observable, subscriber);
    }
    public void autoAskSendOrder(Map<String, String> options, Subscriber<InvSendOrderItemBrief> subscriber) {
        InvSkuStoreService mfhApi = RxHttpManager.createService(InvSkuStoreService.class);
        Observable observable = mfhApi.autoAskSendOrder(options)
                .map(new MResponseFunc<InvSendOrderItemBrief>());
        toSubscribe(observable, subscriber);
    }

}
