package com.mfh.framework.rxapi.http;

import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.rxapi.entity.MResponse;
import com.mfh.framework.rxapi.func.MResponseFunc;

import java.util.List;
import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by bingshanguxue on 25/01/2017.
 */

public class CommonUserAccountHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final CommonUserAccountHttpManager INSTANCE = new CommonUserAccountHttpManager();
    }

    //获取单例
    public static CommonUserAccountHttpManager getInstance() {
        return CommonUserAccountHttpManager.SingletonHolder.INSTANCE;
    }

    private interface CommonUserAccountService{
        /**pos端提交客户编号、订单基础信息和卡券信息，计算金额*/
        @GET("commonuseraccount/getPayAmountByOrderInfos")
        Observable<MResponse<List<PayAmount>>> getPayAmountByOrderInfos(@QueryMap Map<String, String> options);
        /**
         * pos端直接使用满分账户进行余额支支付或积分支付，无具体业务背景:
         * <ol>
         * <li>满分余额支付</li>
         * /commonuseraccount/payDirect?humanId=94182&accountPassword=196735&amount=100000&bizType=3&orderId=123
         * <li>满分扫码积分兑换</li>
         * /commonuseraccount/payDirect?cardNo=245245245254254&score=1000
         * </ol>
         * <p>
         * (若mineCps参数不为空，则相当于支付完毕同时将其废弃，避免再调用一次下面的abandonCouponById接口)
         */
        @GET("commonuseraccount/payDirect")
        Observable<MResponse<String>> payDirect(@QueryMap Map<String, String> options);
    }

    public void getPayAmountByOrderInfos(Map<String, String> options, Subscriber<List<PayAmount>> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.getPayAmountByOrderInfos(options)
                .map(new MResponseFunc<List<PayAmount>>());
        toSubscribe(observable, subscriber);
    }

    public void payDirect(Map<String, String> options, Subscriber<String> subscriber) {
        CommonUserAccountService mfhApi = RxHttpManager.createService(CommonUserAccountService.class);
        Observable observable = mfhApi.payDirect(options)
                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }


}
