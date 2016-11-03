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
    public static void prePay(Long configId, Long humanId, String amount, int wayType, String nonceStr,
                              AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("configId", String.valueOf(configId));
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//
        params.put("wayType", String.valueOf(wayType));
        params.put("wxopenid", String.valueOf(humanId));

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY, params, responseCallback);
    }

    /**
     * 充值预支付(app端微信支付)
     * @param configId 使用哪一个微信支付的编号默认为空后台自动根据当前生态租户选取。
     * @param humanId 人员编号
     * @param amount 充值金额(充值金额必须为数字！单位为元，最小金额为0.01元。)
     * @param wayType 支付途径(1-支付宝 2-微信 21-app端调用微信支付),可不填，默认为2-微信
     * @param nonceStr 随机字符串（32位,不能为空!）
     * @param bizType 业务类型,默认是99充值,商城订单支付是3
     * */
    public static void prePayForApp(Long configId, Long humanId, String amount,
                                    int wayType, String nonceStr, Integer bizType,
                                    AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("configId", String.valueOf(configId));
        params.put("humanId", String.valueOf(humanId));
        params.put("amount", amount);
        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("wayType", String.valueOf(wayType));
        // {"code":"1","msg":"For input string: \"null\"","data":null,"version":1}
        if (bizType != null){
            params.put("bizType", String.valueOf(bizType));
        }

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY_APP, params, responseCallback);
    }


    /**
     * 订单预支付
     * @param humanId 人员编号
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * @param wayType 支付途径 {@link com.mfh.framework.api.constant.WayType}
     *
     * */
    public static void prePayOrder(Long humanId, int wayType, Long configId,
                                   String orderIds, int btype, String nonceStr,
                                   AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("wayType", String.valueOf(wayType));
        params.put("configId", String.valueOf(configId));
//            //微信jsapi
//            params.put("wxopenid", String.valueOf(humanId));

        params.put("nonceStr", nonceStr);//随机字符串（32位,不能为空!）
        params.put("orderIds", orderIds);
        params.put("btype", String.valueOf(btype));

        AfinalFactory.postDefault(PayApi.URL_PRE_PAY_ORDER, params, responseCallback);
    }

}
