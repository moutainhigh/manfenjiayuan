package com.mfh.framework.api.pmcstock;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 14/10/2016.
 */

public class PmcStockApi {
    public static String URL_PMCSTOCK = MfhApi.URL_BASE_SERVER + "/pmcstock/";

    /**
     * 包裹出库操作(支持批量)
     */
    static String URL_STOCK_OUT = URL_PMCSTOCK + "stockOut";


    /**
     * 查找可出库的或已出库的包裹列表
     */
    static String URL_STOCK_FIND_STOCKOUT = URL_PMCSTOCK + "findStockOut";

    /**
     * 查询用户：/pmc/pmcstock/findHumanBySecret?secret=4645&stockId=1203,1207,1209
     */
    static String URL_STOCK_FINDHUMAN_BYSECRET = URL_PMCSTOCK + "findHumanBySecret";


    /**
     * 查询订单列表 /pmcstock/findGoodsOrderList?orderStatus=4&btype=7
     */
    public static String URL_FIND_GOODS_ORDERLIST = URL_PMCSTOCK + "findGoodsOrderList";

    /**
     * pos端提交客户编号和订单基础信息获取可用卡券
     * /pmcstock/findConpousByOrderInfo?humanId=..&jsonStr={productId:[1,2,3], officeId:.., orderAmount:...}
     */
    static String URL_FINDCOUPONS_BYORDERINFO = URL_PMCSTOCK + "findConpousByOrderInfo";


    /**
     * 根据订单编号，查询订单基本信息和详情包括订单的商品明细
     * /pmcstock/getGoodsOrderListByHuman?id=
     */
    static String URL_FINDGOODSORDERLIST_BYHUMAN = URL_PMCSTOCK + "getGoodsOrderListByHuman";

    /**
     * 预支付（订单支付)--微信/支付宝
     */
    static String URL_PRE_PAY_ORDER = URL_PMCSTOCK + "prePayOrder";

    public static void register() {
        URL_PMCSTOCK = MfhApi.URL_BASE_SERVER + "/pmcstock/";
        URL_STOCK_OUT = URL_PMCSTOCK + "stockOut";

        URL_STOCK_FIND_STOCKOUT = URL_PMCSTOCK + "findStockOut";

        URL_STOCK_FINDHUMAN_BYSECRET = URL_PMCSTOCK + "findHumanBySecret";

        URL_FIND_GOODS_ORDERLIST = URL_PMCSTOCK + "findGoodsOrderList";
        URL_FINDCOUPONS_BYORDERINFO = URL_PMCSTOCK + "findConpousByOrderInfo";
        URL_FINDGOODSORDERLIST_BYHUMAN = URL_PMCSTOCK + "getGoodsOrderListByHuman";
        URL_PRE_PAY_ORDER = URL_PMCSTOCK + "prePayOrder";
    }

}
