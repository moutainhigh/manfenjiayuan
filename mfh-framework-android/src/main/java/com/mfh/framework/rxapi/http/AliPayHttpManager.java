package com.mfh.framework.rxapi.http;

import com.mfh.framework.rxapi.entity.MResponse;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import rx.Subscriber;

/**
 * 支付宝支付
 * Created by bingshanguxue on 25/01/2017.
 */

public class AliPayHttpManager extends BaseHttpManager{
    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final AliPayHttpManager INSTANCE = new AliPayHttpManager();
    }

    //获取单例
    public static AliPayHttpManager getInstance() {
        return AliPayHttpManager.SingletonHolder.INSTANCE;
    }

    private interface AliPayService{
        /**
         * 微信条码支付
         *
         * @param outTradeNo         商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
         * @param authCode           支付授权码,用户支付宝钱包中的“付款码”信息
         * @param totalAmount        订单总金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
         * @param discountableAmount 可打折金额
         * @param subject            订单标题
         */
        @GET("toAlipayBarTradePay/barPay")
        Observable<MResponse<String>> wepayBarPay(@QueryMap Map<String, String> options);
        /**
         * 查询订单支付状态
         *
         * @param out_trade_no         商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
         * @param chId           渠道编号
         */
        @GET("toAlipayBarTradePay/query")
        Observable<MResponse<String>> query(@QueryMap Map<String, String> options);
        /**
         * 取消订单
         *
         * @param out_trade_no         商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
         * @param chId           渠道编号
         */
        @GET("toAlipayBarTradePay/cancelOrder")
        Observable<MResponse<String>> cancelOrder(@QueryMap Map<String, String> options);
    }

    public void wepayBarPay(Map<String, String> options, Subscriber<MResponse<String>> subscriber) {
        AliPayService mfhApi = RxHttpManager.createService(AliPayService.class);
        Observable observable = mfhApi.wepayBarPay(options);
//                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void query(Map<String, String> options, Subscriber<MResponse<String>> subscriber) {
        AliPayService mfhApi = RxHttpManager.createService(AliPayService.class);
        Observable observable = mfhApi.query(options);
//                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }
    public void cancelOrder(Map<String, String> options, Subscriber<MResponse<String>> subscriber) {
        AliPayService mfhApi = RxHttpManager.createService(AliPayService.class);
        Observable observable = mfhApi.cancelOrder(options);
//                .map(new MResponseFunc<String>());
        toSubscribe(observable, subscriber);
    }

}
