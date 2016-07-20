package com.mfh.framework.api.impl;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.net.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 4/26/16.
 */
public class MfhApiImpl  extends MfhApi {
    /**
     * 设备注册
     * http://devnew.manfenjiayuan.cn/pmc/posRegister/create?jsonStr={serialNo=222222}
     */
    public static void posRegisterCreate(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(MfhApi.URL_POS_REGISTER_CREATE, params, responseCallback);
    }

    public static void posRegisterUpdate(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(MfhApi.URL_POS_REGISTER_UPDATE, params, responseCallback);
    }
}
