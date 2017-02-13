package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.invLossOrder.InvLossOrder;
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

public class InvLossOrderHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvLossOrderHttpManager INSTANCE = new InvLossOrderHttpManager();
    }

    //获取单例
    public static InvLossOrderHttpManager getInstance() {
        return InvLossOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvLossOrderService{
        /**获取当前网点正在报损的报损单号*/
        @GET("invLossOrder/getCurrentOrder")
        Observable<MResponse<InvLossOrder>> getCurrentOrder(@QueryMap Map<String, String> options);
    }

    public void getCurrentOrder(Map<String, String> options, Subscriber<InvLossOrder> subscriber) {
        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
        Observable observable = mfhApi.getCurrentOrder(options)
                .map(new MResponseFunc<InvLossOrder>());
        toSubscribe(observable, subscriber);
    }



}
