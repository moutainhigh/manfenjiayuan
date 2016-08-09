package com.mfh.framework.api;

import com.mfh.framework.net.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 商超
 * Created by bingshanguxue on 4/19/16.
 */
public class ScApi {
    /**搜索小区*/
    public static final String URL_SUBDIST_LIST = MfhApi.URL_BASE_SERVER + "/subdist/list";
    /**摇一摇·店铺*/
    public final static String URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";


    /**
     * 搜索小区
     * */
    public static void listSubdis(String cityId, String subdisName,
                                        AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("cityID", cityId);
        params.put("subdisName", subdisName);

        AfinalFactory.postDefault(URL_SUBDIST_LIST, params, responseCallback);
    }
}
