package com.mfh.framework.api;

import com.mfh.framework.net.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 价签
 * Created by bingshanguxue on 4/19/16.
 */
public class InvSkuLabelApi {

    public static final String URL_INVSKULABEL = MfhApi.URL_BASE_SERVER + "/invSkuLabel/";
    /**创建待打印价签*/
    public final static String URL_CREATE = URL_INVSKULABEL + "create";

    /**
     * 搜索小区
     * */
    public static void create(String jsonStr,
                                        AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_CREATE, params, responseCallback);
    }
}
