package com.mfh.framework.api.pay;

import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 10/10/2016.
 */

public class PayApiImpl extends PayApi {
    /**
     * 预支付(充值)
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     * @param nonceStr 随机字符串（32位,不能为空!）
     * */
    public static void prePay(Long humanId, String amount, int wayType, String nonceStr,
                              AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//
        params.put("wayType", String.valueOf(wayType));
        params.put("wxopenid", String.valueOf(humanId));

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY, params, responseCallback);
    }

    /**
     * 预支付(充值)
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     * @param nonceStr 随机字符串（32位,不能为空!）
     * */
    public static void prePayForApp(Long humanId, String amount, int wayType, String nonceStr,
                                    AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("wayType", String.valueOf(wayType));
        if(wayType == WAYTYPE_WXPAY){
            params.put("configId", String.valueOf(WX_PAY_CONFIG_ID));
        }else{
            params.put("wxopenid", String.valueOf(humanId));
        }

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY_APP, params, responseCallback);
    }


    /**
     * 满分家园账户充值
     * @param tradeNo 交易号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param token
     *
     * */
    public static void mfhAccountPay(String tradeNo, String orderIds, int btype,
                                     String token,
                                     AjaxCallBack<? extends Object> responseCallback){

        AjaxParams params = new AjaxParams();
        params.put("preOrderId", tradeNo);
        params.put("orderId", orderIds);
        params.put("token", token);
        params.put("btype", String.valueOf(btype));

        AfinalFactory.postDefault(URL_MF_ACCOUNT_PAY, params, responseCallback);
    }

    /**
     * 订单预支付
     * @param humanId 人员编号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     *
     * */
    public static void prePayOrder(Long humanId, String orderIds, int btype,
                                   int wayType, String nonceStr,
                                   AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("wayType", String.valueOf(wayType));
        if(wayType == WAYTYPE_ALIPAY){
            params.put("wxopenid", String.valueOf(humanId));
        }else if(wayType == WAYTYPE_WXPAY){
            params.put("configId", String.valueOf(PayApi.WX_PAY_CONFIG_ID));
        }else{
            params.put("wxopenid", String.valueOf(humanId));
        }

        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("orderIds", orderIds);
        params.put("btype", String.valueOf(btype));

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY_ORDER, params, responseCallback);
    }

}
