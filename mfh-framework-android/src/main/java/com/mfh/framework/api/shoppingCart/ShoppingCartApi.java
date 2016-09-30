package com.mfh.framework.api.shoppingCart;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 9/29/16.
 */

public class ShoppingCartApi {
    public static final String URL_SHOPPING_CART = MfhApi.URL_BASE_SERVER + "shoppingCart/";


    /**
     * 购物车列表
     * */
    public static final String URL_LIST = URL_SHOPPING_CART + "list";
    /**
     * 修改购物项数量
     * */
    public static final String URL_ADJUST_CART= URL_SHOPPING_CART + "adjustCart";

}
