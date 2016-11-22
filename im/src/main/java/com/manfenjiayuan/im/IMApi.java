package com.manfenjiayuan.im;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.utils.IMFactory;
import com.mfh.framework.api.ApiParams;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 16/3/1.
 */
public class IMApi implements ApiParams {
    //注册消息桥
    public static String URL_MOBILE_MESSAGE = NetFactory.getRegisterMessageUrl();
    //会话
    public static String URL_GET_SESSION_BY_ID = MfhApi.URL_BASE_SERVER + "/biz/msg/getSessionById";
    public static String URL_SESSION_LIST = "/biz/msg/getSessionList";


    public static void register(){
        URL_GET_SESSION_BY_ID = MfhApi.URL_BASE_SERVER + "/biz/msg/getSessionById";
        URL_SESSION_LIST = "/biz/msg/getSessionList";
    }

    /**
     * 注册消息桥
     * @param guid
     * @param clientId
     * @param responseCallback
     * */
    public static void registerBridge(Long guid, String clientId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_CHANNEL_ID, MfhApi.CHANNEL_ID);
        params.put(PARAM_KEY_QUEUENAME, MfhApi.PARAM_VALUE_QUEUE_NAME_DEF);
        params.put(PARAM_KEY_JSONSTR, JSON.toJSONString(IMFactory.register(clientId, guid)));
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_MOBILE_MESSAGE, params, responseCallback);
    }
}
