package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.rxapi.bean.InvSendIoOrderBody;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MValue;
import com.mfh.framework.rxapi.func.MValueResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvSendIoOrderHttpManager extends BaseHttpManager {
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvSendIoOrderHttpManager INSTANCE = new InvSendIoOrderHttpManager();
    }

    //获取单例
    public static InvSendIoOrderHttpManager getInstance() {
        return InvSendIoOrderHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvSendIoOrderService{
        /**
         * 由门店自行创建收货单（此时因批发商不在平台上故没办法发货）
         * /invSendIoOrder/createRecOrder?otherOrderId=&checkOk=true&jsonStr={"receiveNetId":132079, "tenantId":130222, "sendTenantId":222, items:[{"chainSkuId":111,"proSkuId":1264,"productName":"狗粮", "giveCount":0,"totalCount":2, "price":11, "amount":22, "barcode":"32131234524"}, {"proSkuId":1266,"productName":"蒜泥生菜", "totalCount":3, "price":5, "amount":15, "barcode":"qu75745555"}]}
         * 其中otherOrderId为采购单，此处可以为空;
         * checkOk代表是否直接自动审核通过并生成入库单,进一步地如果收货网点就是当前操作用户的登录网点，则还会自动对生成的入库单进行入库操作。
         * sendNetId参数不要传递，sendTenantId可以从采购单中拷贝，本身也可以置空；
         * receiveNetId、tenantId可以从采购单中拷贝，没有采购单则可以置空，若置空后台也会自动根据当前登录用户的部门和租户进行填充。
         * items是发货单明细，字段与采购单明细字段基本一致，从采购单中拷贝即可，其中giveCount是赠送数量。没有采购单时需要从头选择商品。
         *
         * 成功返回：{"code":"0","msg":"新增成功!","version":"1","data":{"val":"2638"}}
         */
//        @GET("invSendIoOrder/createRecOrder")
//        Observable<MResponse<MValue<String>>> createRecOrder(@QueryMap Map<String, String> options);
        @POST("invSendIoOrder/createRecOrder")
        Observable<MResponse<MValue<String>>> createRecOrder(@QueryMap Map<String, String> options);

        @POST("invSendIoOrder/createRecOrder")
        Observable<MResponse<MValue<String>>> createRecOrder2(@Query("JSESSIONID") String JSESSIONID,
                                                             @Body InvSendIoOrderBody body);

//        Observable<MResponse<String>> createRecOrder(@QueryMap Map<String, String> options);


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

    public void createRecOrder(Map<String, String> options, MValueSubscriber<String> subscriber) {
        InvSendIoOrderService mfhApi = RxHttpManager.createService(InvSendIoOrderService.class);
        Observable observable = mfhApi.createRecOrder(options)
                .map(new MValueResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

    public void createRecOrder2(String JSESSIONID, InvSendIoOrderBody body, MValueSubscriber<String> subscriber) {
        InvSendIoOrderService mfhApi = RxHttpManager.createService(InvSendIoOrderService.class);
        Observable observable = mfhApi.createRecOrder2(JSESSIONID, body)
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
