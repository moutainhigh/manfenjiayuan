package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
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
        /**
         * 获取当前网点正在报损的报损单号,如果没有则直接创建。
         * invLossOrder/getCurrentOrder?netId=&storeType=
         * netId 默认为当前登录网点；storeType 0-代表零售仓
         * */
        @GET("invLossOrder/getCurrentOrder")
        Observable<MResponse<InvLossOrder>> getCurrentOrder(@QueryMap Map<String, String> options);

        /**
         * 获取当前网点正在报损的报损单号,如果没有则直接创建。
         * invLossOrder/getCurrentOrder?netId=&storeType=
         * netId 默认为当前登录网点；storeType 0-代表零售仓
         * */
        @GET("invLossOrder/list")
        Observable<MResponse<MRspQuery<MEntityWrapper<InvLossOrder>>>> list(@QueryMap Map<String, String> options);

        /**
         * 针对一个报损单，提交一批报损记录,需要登录。
         * /invLossOrderItem/batchCommitItems?orderId=21&posId=1&jsonStr=
         * [{"barcode":"6925303770594","quantityCheck":11, "updateHint":1}, {"barcode":"6921168509256","quantityCheck":11, "updateHint":1}]
         *
         * {"code":"0","msg":"操作成功!","version":"1","data":""}
         *
         */
        @GET("invLossOrderItem/batchCommitItems")
        Observable<MResponse<String>> batchCommitItems(@QueryMap Map<String, String> options);

//        /**
//         * 库存报损订单明细列表
//         */
//        @GET("invLossOrderItem/list")
//        Observable<MResponse<InvLossOrder>> list(@QueryMap Map<String, String> options);
    }

    public void getCurrentOrder(Map<String, String> options, Subscriber<InvLossOrder> subscriber) {
        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
        Observable observable = mfhApi.getCurrentOrder(options)
                .map(new MResponseFunc<InvLossOrder>());
        toSubscribe(observable, subscriber);
    }

    public void list(Map<String, String> options,
                                    MQuerySubscriber<MEntityWrapper<InvLossOrder>> subscriber) {
        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<MEntityWrapper<InvLossOrder>>());
        toSubscribe(observable, subscriber);
    }

    public void batchCommitItems(Map<String, String> options, Subscriber<String> subscriber) {
        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
        Observable observable = mfhApi.batchCommitItems(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }



}
