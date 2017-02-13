package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.scOrder.ScOrder;
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

public class ScOrderHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final ScOrderHttpManager INSTANCE = new ScOrderHttpManager();
    }

    //获取单例
    public static ScOrderHttpManager getInstance() {
        return ScOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface ScOrderService{
        /**
         * 查询订单
         * /scOrder/getByCode?barcode=9903000000273899
         */
        @GET("scOrder/getByCode")
        Observable<MResponse<ScOrder>> getByCode(@QueryMap Map<String, String> options);

    }

    public void getByCode(Map<String, String> options, Subscriber<ScOrder> subscriber) {
        ScOrderService mfhApi = RxHttpManager.createService(ScOrderService.class);
        Observable observable = mfhApi.getByCode(options)
                .map(new MResponseFunc<ScOrder>());
        toSubscribe(observable, subscriber);
    }



}
