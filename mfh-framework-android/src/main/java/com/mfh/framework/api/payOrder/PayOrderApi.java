package com.mfh.framework.api.payOrder;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 04/11/2016.
 */

public class PayOrderApi {
    public static String URL_PAYORDER = MfhApi.URL_BASE_SERVER + "/payOrder/";

    /**
     * 预支付(充值)--支付宝app支付
     */
    static String URL_PREPAY = URL_PAYORDER + "prepay";
    /**
     * 预支付(充值)——微信app支付
     */
    static String URL_PREPAY_FORAPP = URL_PAYORDER + "prepayForApp";
    /**
     * 银联支付后调用后台  /payOrder/create?jsonStr={}
     */
    static String URL_CREATE = URL_PAYORDER + "create";
    /**
     * 打款明细：/payOrder/list?status=2&sellOffice=136076&bizType=2001
     */
    public static String URL_LIST = URL_PAYORDER + "list";

    public static void register() {
        URL_PAYORDER = MfhApi.URL_BASE_SERVER + "/payOrder/";

        URL_PREPAY = URL_PAYORDER + "prepay";
        URL_PREPAY_FORAPP = URL_PAYORDER + "prepayForApp";
        URL_CREATE = URL_PAYORDER + "create";
        URL_LIST = URL_PAYORDER + "list";
    }
}
