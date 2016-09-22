package com.mfh.enjoycity.utils;

import com.mfh.framework.api.MfhApi;

/**
 * 城市之间－－API接口
 * Created by NAT.ZZN(bingshanguxue) on 2015/8/12.
 */
public class EnjoycityApi {
    //查询周边小区
    public static final String URL_FIND_ARROUND_SUBDIST = MfhApi.URL_BASE_SERVER + "/subdist/findArroundSubdist";
    /**查询周边店铺*/
    public static final String URL_FIND_ARROUND_MARKET_SHOPS = MfhApi.URL_BASE_SERVER + "/scShop/findArroundMarketShops";
    //搜索小区
//    public static final String URL_QUERY_SUBDIST_LIST = MfhApi.URL_BASE_SERVER + "/subdist/list";
    /**新增收货地址*/
    public final static String URL_CREATE_RECEIVE_ADDRESS = MfhApi.URL_BASE_SERVER + "/reciaddr/createForHuman";
    /**查询收货地址*/
    public final static String URL_QUERYALL_RECEIVE_ADDRESS = MfhApi.URL_BASE_SERVER + "/reciaddr/getAllAddrsByHuman";


    //(商城/洗衣)订单支付
    public final static String URL_ORDER_ACCOUNT_PAY = MfhApi.URL_BASE_SERVER + "/commonuseraccount/scAccountPay";
    //热卖商品
    public final static String URL_SHOP_HOT_SALES = MfhApi.URL_BASE_SERVER + "/scShopHotSale/listShopHotSales";
    //商品详情
    public final static String URL_PRODUCT_DETAIL = MfhApi.URL_BASE_SERVER + "/anon/sc/product/getById";
    /**类目查询－－一级类目*/
    public final static String URL_CATEGORYINFO_COMNQUERY = MfhApi.URL_BASE_SERVER + "/scCategoryInfo/comnQuery";

    //商品查询（查询类目商品）
    public final static String URL_FIND_PRODUCT = MfhApi.URL_BASE_SERVER + "/anon/sc/product/findProduct";
    //我常买
    public final static String URL_OFEN_BUY_PRODUCTS= MfhApi.URL_BASE_SERVER + "/anon/sc/product/mineBuysProductList";

    /**优惠券*/
    public final static String URL_FIND_COUPONS = MfhApi.URL_BASE_SERVER + "/pmcstock/findConpousByOrderId";
    /**商品促销标签*/
    public final static String URL_FIND_PROMOTE_LABEL = MfhApi.URL_BASE_SERVER + "/scShopPmLabel/listProductsLabel";
    /**商品促销价格*/
    public final static String URL_FIND_PROMOTE_PRICE = MfhApi.URL_BASE_SERVER + "/scShopPromotion/listProductPrices";

    /**业务类型，1是洗衣，6是皮具 3-是商城 7是商超*/
    public final static int BTYPE_MALL = 7;//商超
    public final static int BTYPE_STORE = 3;//商城

    /**支付方式*/
    public final static int PAY_WAYTYPE_ALIPAY = 32;//支付宝
    public final static int PAY_WAYTYPE_WX = 512;//微信

    //123满分家园，124满分小伙伴，125城市之间
    public final static int WX_PAY_CONFIG_ID = 125;

      //摇一摇·店铺
    public final static String URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";

    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String ALIPAY_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";
    public static final String ALIPAY_ORDER_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/pmcstock/notifyOrder";


}
