package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvSendOrderHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvSendOrderHttpManager INSTANCE = new InvSendOrderHttpManager();
    }

    //获取单例
    public static InvSendOrderHttpManager getInstance() {
        return InvSendOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvSendOrderService{
        /**
         * <b>需要登录</b>
         * 由门店方创建一个采购单，需要登录,采购单的tenantId就是门店的tenantId
         * 适用场景：门店智能订货，标品订货，生鲜订货
         *
         * 成功返回： {"code":"0","msg":"新增成功!","version":"1","data":{"val":"779"}}
         */
        @GET("invSendOrder/askSendOrder")
        Observable<MResponse<MValue<String>>> askSendOrder(@QueryMap Map<String, String> options);


//        /**
//         * 获取当前网点正在报损的报损单号,如果没有则直接创建。
//         * invLossOrder/getCurrentOrder?netId=&storeType=
//         * netId 默认为当前登录网点；storeType 0-代表零售仓
//         * */
//        @GET("invLossOrder/list")
//        Observable<MResponse<MRspQuery<MEntityWrapper<InvLossOrder>>>> list(@QueryMap Map<String, String> options);
//
//        /**
//         * 针对一个报损单，提交一批报损记录,需要登录。
//         * /invLossOrderItem/batchCommitItems?orderId=21&posId=1&jsonStr=
//         * [{"barcode":"6925303770594","quantityCheck":11, "updateHint":1}, {"barcode":"6921168509256","quantityCheck":11, "updateHint":1}]
//         */
//        @GET("invLossOrderItem/batchCommitItems")
//        Observable<MResponse<String>> batchCommitItems(@QueryMap Map<String, String> options);

//        /**
//         * 库存报损订单明细列表
//         */
//        @GET("invLossOrderItem/list")
//        Observable<MResponse<InvLossOrder>> list(@QueryMap Map<String, String> options);
    }

    public void askSendOrder(Map<String, String> options, MValueSubscriber<String> subscriber) {
        InvSendOrderService mfhApi = RxHttpManager.createService(InvSendOrderService.class);
        Observable observable = mfhApi.askSendOrder(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

//    public void list(Map<String, String> options,
//                                    MQuerySubscriber<MEntityWrapper<InvLossOrder>> subscriber) {
//        InvSendIoOrderService mfhApi = RxHttpManager.createService(InvSendIoOrderService.class);
//        Observable observable = mfhApi.list(options)
//                .map(new MQueryResponseFunc<MEntityWrapper<InvLossOrder>>());
//        toSubscribe(observable, subscriber);
//    }
//
//    public void batchCommitItems(Map<String, String> options, Subscriber<String> subscriber) {
//        InvSendIoOrderService mfhApi = RxHttpManager.createService(InvSendIoOrderService.class);
//        Observable observable = mfhApi.batchCommitItems(options)
//                .map(new MResponseFunc<String>());
//        toSubscribe(observable, subscriber);
//    }



}
