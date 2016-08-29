package com.mfh.framework.api;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 支付API
 * Created by bingshanguxue on 4/21/16.
 */
public class PayApi {
    /**预支付(充值)--支付宝*/
    public final static String URL_PRE_PAY = MfhApi.URL_BASE_SERVER + "/payOrder/prepay";
    /**预支付(充值)--微信*/
    public final static String URL_PRE_PAY_APP = MfhApi.URL_BASE_SERVER + "/payOrder/prepayForApp";
    /**预支付（订单支付)--微信/支付宝*/
    public final static String URL_PRE_PAY_ORDER = MfhApi.URL_BASE_SERVER + "/pmcstock/prePayOrder";

    /**满分账户充值*/
    public final static String URL_MF_ACCOUNT_PAY = MfhApi.URL_BASE_SERVER + "/commonuseraccount/mfhAccountPay";

    /**
     * 支付宝条码支付请求接口：
     /toAlipayBarTradePay/barPay?jsonStr={out_trade_no:20150929003638,auth_code:289802075510210664,total_amount:0.1,subject:test,terminal_id:001,operator_id:112369}*/
    public static final String URL_ALIPAY_BARPAY = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/barPay";
    public static final String URL_ALIPAY_QUERY = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/query";
    public static final String URL_ALIPAY_CANCEL = MfhApi.URL_BASE_SERVER + "/toAlipayBarTradePay/cancelOrder";

    /**
     * 微信条码支付请求接口：
     /toAlipayBarTradePay/barPay?jsonStr={out_trade_no:20150929003638,auth_code:289802075510210664,total_amount:0.1,subject:test,terminal_id:001,operator_id:112369}*/
    public static final String URL_WXBARPAY_PAY = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/barPay";
    public static final String URL_WXBARPAY_QUERY = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/query";
    public static final String URL_WXBARPAY_CANCEL = MfhApi.URL_BASE_SERVER + "/toWxpayBarTradePay/cancelOrder";

    /**
     * 支付宝条码支付
     * @param outTradeNo 商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
     * @param authCode 支付授权码,用户支付宝钱包中的“付款码”信息
     * @param totalAmount 订单总金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param discountableAmount 可打折金额
     * @param subject 订单标题
     * @param bizType 业务类型
     * @param chId 支付渠道编号
     * */
    public static void aliBarPay(String jsonStr,String bizType, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put("bizType", bizType);
        params.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ALIPAY_BARPAY, params, responseCallback);
    }

    public static void queryAliBarpayStatus(String outTradeNo, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("out_trade_no", outTradeNo);
        params.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ALIPAY_QUERY, params, responseCallback);
    }

    public static void cancelAliBarpay(String outTradeNo, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("out_trade_no", outTradeNo);
        params.put("chId", MfhApi.ALIPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_ALIPAY_CANCEL, params, responseCallback);
    }


    /**
     * 微信条码支付
     * @param outTradeNo 商户订单号,商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。
     * @param authCode 支付授权码,用户支付宝钱包中的“付款码”信息
     * @param totalAmount 订单总金额,单位为元，精确到小数点后两位，取值范围[0.01,100000000]，
     * @param discountableAmount 可打折金额
     * @param subject 订单标题
     * */
    public static void wxBarPay(String jsonStr,String bizType, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);
        params.put("bizType", bizType);
        params.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_WXBARPAY_PAY, params, responseCallback);
    }
    public static void queryWxBarpayStatus(String outTradeNo, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("out_trade_no", outTradeNo);
        params.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_WXBARPAY_QUERY, params, responseCallback);
    }

    public static void cancelWxBarpay(String outTradeNo, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("out_trade_no", outTradeNo);
        params.put("chId", MfhApi.WXPAY_CHANNEL_ID);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_WXBARPAY_CANCEL, params, responseCallback);
    }

}
