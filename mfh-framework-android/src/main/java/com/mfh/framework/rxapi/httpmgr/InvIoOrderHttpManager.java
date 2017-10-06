package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MResponseFunc;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvIoOrderHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvIoOrderHttpManager INSTANCE = new InvIoOrderHttpManager();
    }

    //获取单例
    public static InvIoOrderHttpManager getInstance() {
        return InvIoOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvIoOrderService{
        /**
         * 创建一个针对本网点仓库的出入库单
         * /invIoOrder/createIoOrder
         * <ol>
         *     <li>成功返回 {"code":"0","msg":"操作成功!","version":"1","data":{"val":"4003"}}</li>
         * </ol>
         *
         */
        @GET("invIoOrder/createIoOrder")
        Observable<MResponse<MValue<String>>> createIoOrder(@QueryMap Map<String, String> options);


        /**
         * 提交一个出入库单，正式出入库,如果是出库单可以进一步填写物流信息如车辆、司机（可空）。
         * /invIoOrder/commitOrder
         *
         * <ol>
         *     <li>成功返回 {"code":"0","msg":"操作成功!","version":"1","data":""}</li>
         * </ol>
         */
        @GET("invIoOrder/commitOrder")
        Observable<MResponse<String>> commitOrder(@QueryMap Map<String, String> options);
    }

    public void createIoOrder(Map<String, String> options, MValueSubscriber<String> subscriber) {
        InvIoOrderService mfhApi = RxHttpManager.createService(InvIoOrderService.class);
        Observable observable = mfhApi.createIoOrder(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void commitOrder(Map<String, String> options, Subscriber<String> subscriber) {
        InvIoOrderService mfhApi = RxHttpManager.createService(InvIoOrderService.class);
        Observable observable = mfhApi.commitOrder(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }



}
