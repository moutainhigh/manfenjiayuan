package com.mfh.framework.api.sms;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 短信验证码
 * Created by bingshanguxue on 11/10/2016.
 */

public class HumanAuthTempApi {
    public static String URL_HUMANAUTHTEMP = MfhApi.URL_BASE_SERVER + "/humanAuthTemp/";

    /**
     * 首次对某个临时用户发送手机短信验证码
     * /embWxUserRegister/humanAuthTemp?mobile=&sourceType=10, 注意返回一个userTmpId备用
     */
    private static String URL_BEGINAUTHENBYSMS = URL_HUMANAUTHTEMP + "beginAuthenBysms";

    /**
     * 首次对某个临时用户发送手机短信验证码
     * /embWxUserRegister/retryAuthenBysms?mobile=&sourceType=10, 注意返回一个userTmpId备用
     */
    private static String URL_RETRYAUTHENBYSMS = URL_HUMANAUTHTEMP + "retryAuthenBysms";

    /**
     * 首次对某个临时用户发送手机短信验证码
     * /embWxUserRegister/retryAuthenBysms?mobile=&sourceType=10, 注意返回一个userTmpId备用
     */
    public static String URL_LOGINBYSMS = MfhApi.URL_BASE_SERVER + "/loginBySms";


    /**
     * 首次对某个临时用户发送手机短信验证码
     *
     * @param mobile 手机号
     * @see HumanAuthTempApi#URL_BEGINAUTHENBYSMS
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
     * @see HumanAuthTempApi#URL_RETRYAUTHENBYSMS
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
     * @param token     验证码
     * @param userTmpId 临时用户id
     * @see HumanAuthTempApi#URL_LOGINBYSMS
     * <p>
     * {"code":"1","msg":"缺少渠道端点标识！","data":null,"version":1}
     */
    public static void loginBySms(String token, Long userTmpId,
                                  AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("userTmpId", userTmpId);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonObject.toJSONString());
        params.put("loginKind", "humanId");

//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LOGINBYSMS, params, responseCallback);
    }

}
