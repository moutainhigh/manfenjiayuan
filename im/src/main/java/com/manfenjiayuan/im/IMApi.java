package com.manfenjiayuan.im;

import com.mfh.framework.api.ApiParams;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.NetFactory;

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

}
