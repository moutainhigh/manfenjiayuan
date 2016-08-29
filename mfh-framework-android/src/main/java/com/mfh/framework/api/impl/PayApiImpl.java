package com.mfh.framework.api.impl;

import com.mfh.framework.api.PayApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 5/23/16.
 */
public class PayApiImpl extends PayApi{
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

}
