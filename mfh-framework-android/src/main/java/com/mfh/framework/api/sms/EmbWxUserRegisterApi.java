package com.mfh.framework.api.sms;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 11/10/2016.
 */

public class EmbWxUserRegisterApi {
    public static String URL_EMB_WXUSER_REGISTER = MfhApi.URL_BASE_SERVER + "/embWxUserRegister/";

    /**
     * 首次对某个临时用户发送手机短信验证码
     * /embWxUserRegister/beginAuthenBysms?mobile=&sourceType=10, 注意返回一个userTmpId备用
     */
    private static String URL_BEGINAUTHENBYSMS = URL_EMB_WXUSER_REGISTER + "beginAuthenBysms";

    /**
     * 对某个临时用户重新发送手机短信验证码
     * /embWxUserRegister/retryAuthenBysms?mobile=&userTmpId=
     */
    private static String URL_RETRYAUTHENBYSMS = URL_EMB_WXUSER_REGISTER + "retryAuthenBysms";

    /**
     * 对接收到的手机验证码进行验证。
     * /embWxUserRegister/doAuthenBysms?token=&userTmpId=
     */
    private static String URL_DOAUTHENBYSMS = URL_EMB_WXUSER_REGISTER + "doAuthenBysms";

    /**
     * 首次对某个临时用户发送手机短信验证码
     *
     * @param mobile 手机号
     * @see #URL_BEGINAUTHENBYSMS
     */
    public static void beginAuthenBysms(String mobile, AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", mobile);
        jsonObject.put("sourceType", 10);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BEGINAUTHENBYSMS, params, responseCallback);
    }

    /**
     * 对某个临时用户重新发送手机短信验证码
     *
     * @param mobile 手机号
     * @see #URL_RETRYAUTHENBYSMS
     */
    public static void retryAuthenBysms(String mobile, Long userTmpId,
                                        AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", mobile);
        jsonObject.put("userTmpId", userTmpId);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_RETRYAUTHENBYSMS, params, responseCallback);
    }

    /**
     * 对接收到的手机验证码进行验证
     *
     * @param token 验证码
     * @see #URL_DOAUTHENBYSMS
     */
    public static void doAuthenBysms(String token, Long userTmpId,
                                     AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("userTmpId", userTmpId);
        jsonObject.put("sourceType", 10);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_DOAUTHENBYSMS, params, responseCallback);
    }

}
