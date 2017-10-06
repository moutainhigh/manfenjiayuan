package com.mfh.framework.rxapi.httpmgr;

import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.entity.MRspQuery;
import com.mfh.framework.rxapi.func.MQueryResponseFunc;
import com.mfh.framework.rxapi.http.BaseHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * 批发商租户
 *
 * Created by bingshanguxue on 25/01/2017.
 */

public class InvCompanyHttpManager extends BaseHttpManager {
    //上线
    public static final Integer STATUS_ONLINE = 2;

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final InvCompanyHttpManager INSTANCE = new InvCompanyHttpManager();
    }

    //获取单例
    public static InvCompanyHttpManager getInstance() {
        return InvCompanyHttpManager.SingletonHolder.INSTANCE;
    }

    private interface InvCompanyService{
//        /**
//         * 获取当前网点正在报损的报损单号,如果没有则直接创建。
//         * invLossOrder/getCurrentOrder?netId=&storeType=
//         * netId 默认为当前登录网点；storeType 0-代表零售仓
//         * */
//        @GET("invLossOrder/getCurrentOrder")
//        Observable<MResponse<InvLossOrder>> getCurrentOrder(@QueryMap Map<String, String> options);

        /**
         * 查询批发商租户，
         * /invCompany/list?status=2&shortCode=，shortCode是速记码，status=2代表上线的
         */
        @GET("invCompany/list")
        Observable<MResponse<MRspQuery<CompanyInfo>>> list(@QueryMap Map<String, String> options);

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

//    public void getCurrentOrder(Map<String, String> options, Subscriber<InvLossOrder> subscriber) {
//        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
//        Observable observable = mfhApi.getCurrentOrder(options)
//                .map(new MResponseFunc<InvLossOrder>());
//        toSubscribe(observable, subscriber);
//    }

    public void list(Map<String, String> options,
                                    MQuerySubscriber<CompanyInfo> subscriber) {
        InvCompanyService mfhApi = RxHttpManager.createService(InvCompanyService.class);
        Observable observable = mfhApi.list(options)
                .map(new MQueryResponseFunc<CompanyInfo>());
        toSubscribe(observable, subscriber);
    }

//    public void batchCommitItems(Map<String, String> options, Subscriber<String> subscriber) {
//        InvLossOrderService mfhApi = RxHttpManager.createService(InvLossOrderService.class);
//        Observable observable = mfhApi.batchCommitItems(options)
//                .map(new MResponseFunc<String>());
//        toSubscribe(observable, subscriber);
//    }



}
