package com.mfh.framework.api.shoppingCart;

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
     * 查询购物车
     * @param shopId 店铺编号
     * */
    public static void list(Long shopId, PageInfo pageInfo,
                                                      AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (shopId != null) {
            params.put("shopId", String.valueOf(shopId));
        }
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
     * @param number 商品数量
     * */
    public static void adjustCart(Long id, int number,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("number", String.valueOf(number));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_ADJUST_CART, params, responseCallback);
    }
}
