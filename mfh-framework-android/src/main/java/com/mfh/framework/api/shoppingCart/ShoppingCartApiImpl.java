package com.mfh.framework.api.shoppingCart;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 9/29/16.
 */

public class ShoppingCartApiImpl extends ShoppingCartApi {
    /**
     * 把一个商品及其选择的规格选项加入到当前登录用户的购物车
     * @param cart 商品信息
     * @param specItems 规格选项
     * */
    public static void add2Cart(JSONObject cart, JSONArray specItems,
                                  AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cart", cart);
        jsonObject.put("specItems", specItems);
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_ADD2CART, params, responseCallback);
    }

    /**
     * 查询购物车
     * @param shopId 店铺编号
     * */
    public static void list(Long shopId, Long ownerId, PageInfo pageInfo,
                                                      AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("shopId", String.valueOf(shopId));
        params.put("ownerId", String.valueOf(ownerId));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }
    /**
     * 修改购物项数量
     * @param id 商品编号
     * @param num 商品数量
     * */
    public static void adjustCart(Long id, int num,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("num", String.valueOf(num));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_ADJUST_CART, params, responseCallback);
    }

    /**
     * 查询购物车中商品数量（需要登录）
     * @param shopIds 店铺编号
     * */
    public static void staticShopCart(String shopIds,
                                  AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("shopIds", shopIds);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_STATICSHOPCART, params, responseCallback);
    }
}
