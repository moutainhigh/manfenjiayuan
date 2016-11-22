package com.mfh.framework.api.shoppingCart;

import com.mfh.framework.api.MfhApi;

/**
 * 购物车
 * Created by bingshanguxue on 9/29/16.
 */

public class ShoppingCartApi {
    public static final Integer BIZTYPE_BUY = 0;//购买
    public static final Integer BIZTYPE_RESERVE = 1;//预定
    public static final Integer SUBTYPE = 1;

    public static String URL_SHOPPING_CART = MfhApi.URL_BASE_SERVER + "/shoppingCart/";


    /**
     * 把一个商品及其选择的规格选项加入到当前登录用户的购物车
     */
    static String URL_ADD2CART = URL_SHOPPING_CART + "addToCart";
    /**
     * 购物车列表
     */
    public static String URL_LIST = URL_SHOPPING_CART + "list";
    /**
     * 修改购物项数量
     */
    static String URL_ADJUST_CART = URL_SHOPPING_CART + "adjustCart";
    /**
     * 查询购物车中商品数量
     * /shoppingCart/staticShopCart?shopIds=
     */
    static String URL_STATICSHOPCART = URL_SHOPPING_CART + "staticShopCart";

    public static void register() {
        URL_SHOPPING_CART = MfhApi.URL_BASE_SERVER + "/shoppingCart/";
        URL_ADD2CART = URL_SHOPPING_CART + "addToCart";
        URL_LIST = URL_SHOPPING_CART + "list";
        URL_ADJUST_CART = URL_SHOPPING_CART + "adjustCart";
        URL_STATICSHOPCART = URL_SHOPPING_CART + "staticShopCart";
    }

}
