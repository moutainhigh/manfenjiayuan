package com.mfh.framework.api.anon.sc.productPrice;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScProductPriceApi {
    public static String URL_ANON_SC_PRODUCTPRICE = MfhApi.URL_BASE_SERVER + "/anon/sc/productPrice/";

    /**
     * /anon/sc/productPrice/findProductSku?barcode
     * 查询平台商品档案
     * */
    private static String URL_FIND_PRODUCTSKU= URL_ANON_SC_PRODUCTPRICE + "findProductSku";


    public static void register(){
        URL_ANON_SC_PRODUCTPRICE = MfhApi.URL_BASE_SERVER + "/anon/sc/productPrice/";
        URL_FIND_PRODUCTSKU= URL_ANON_SC_PRODUCTPRICE + "findProductSku";
    }


    /**
     * 根据条码查询平台商品档案
     * @param barcode 商品条码
     */
    public static void findProductSku(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_PRODUCTSKU, params, responseCallback);
    }

}
