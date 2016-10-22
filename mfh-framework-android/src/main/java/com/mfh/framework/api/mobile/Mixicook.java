package com.mfh.framework.api.mobile;

import com.mfh.framework.BizConfig;

/**
 * Created by bingshanguxue on 9/29/16.
 */

public class Mixicook {
    public static String API_BASE_URL = "http://mobile.mixicook.com/mobile/";
    public static String DOMAIN = "mobile.mixicook.com";
    public static String COOKIE_URL = "http://mobile.mixicook.com";
    public static String CUSTOMER_SERVICELCENTER = "15250065084";


    static{
        if(BizConfig.RELEASE){
            API_BASE_URL = "http://mobile.mixicook.com/mobile/";
//            BASE_URL_RESOURCE = "http://resource.manfenjiayuan.cn/user/";
            DOMAIN = "mobile.mixicook.com";
            COOKIE_URL = "http://mobile.mixicook.com";

        }else{
            API_BASE_URL = "http://devmobile.mixicook.com/mobile/";
//            BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
            DOMAIN = "devmobile.mixicook.com";
            COOKIE_URL = "http://devmobile.mixicook.com";
        }
    }

    /**
     * 账户
     * */
    public final static String URL_ME_ACCOUNT = API_BASE_URL + "me/account";
    /**
     * 优惠券
     * */
    public final static String URL_ME_COUPONS = API_BASE_URL + "me/coupons";
    /**
     * 积分
     * */
    public final static String URL_ME_SCORE = API_BASE_URL + "me/score";
    /**
     * 订单
     * */
    public final static String URL_ME_ORDER = API_BASE_URL + "me/order";
    /**
     * 会员卡
     * */
    public final static String URL_ME_CARD = API_BASE_URL + "me/card";
    /**
     * 收货地址
     * */
    public final static String URL_ME_ADDRESS = API_BASE_URL + "me/address";

    /**
     * 店铺 http://mobile.mixicook.com/mobile/market/shop
     * */
    public final static String URL_MARKET_SHOP = API_BASE_URL + "market/shop";
    /**
     * 购物车 http://mobile.mixicook.com/mobile/market/cart
     * */
    public final static String URL_MARKET_CART = API_BASE_URL + "market/cart";

    /**
     * 绑定门店 http://mobile.mixicook.com/mobile/net/shop/search
     * */
    public final static String URL_NET_SHOP_SEARCH = API_BASE_URL + "net/shop/search";




}
