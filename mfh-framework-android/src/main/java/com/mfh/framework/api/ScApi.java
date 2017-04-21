package com.mfh.framework.api;

/**
 * 商超
 * Created by bingshanguxue on 4/19/16.
 */
public class ScApi {
    /**
     * 摇一摇·店铺
     */
    public static String URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";

    public static void register() {
        URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";
    }
}
