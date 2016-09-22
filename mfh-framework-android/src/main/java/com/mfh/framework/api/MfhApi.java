package com.mfh.framework.api;

import com.mfh.framework.BizConfig;
import com.mfh.framework.network.NetFactory;

/**
 * 满分家园后台接口
 * Created by Administrator on 2015/6/11.
 */
public class MfhApi {
    //UBS
    public static String URL_BASE_SERVER = NetFactory.getServerUrl();
    public static String URL_REGISTER_MESSAGE = NetFactory.getRegisterMessageUrl();
    public static String WXPAY_CHANNEL_ID = NetFactory.getWxPayChannelId();
    public static String ALIPAY_CHANNEL_ID = NetFactory.getAliPayChannelId();


    //网络电话
    public static String URL_NET_PHONE = URL_BASE_SERVER.replace(":8080/pmc", "") + "/msgcore/embYtx/getYuninfoByGuid";
    /**登录*/
    public static String URL_LOGIN = URL_BASE_SERVER + "/login";
    /**退出*/
    public static String URL_LOGOUT = URL_BASE_SERVER + "/exit";


    //域名
    public static String DOMAIN = "devmobile.manfenjiayuan.cn";
    public static String URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";

    //渠道编号
    public final static String PARAM_KEY_CHANNEL_ID = "channelid";
    public static String CHANNEL_ID = NetFactory.getChannelId();

    //注册消息桥参数
    public final static String PARAM_KEY_QUEUE_NAME = "queuename";
    public final static String PARAM_KEY_JSONSTR = "jsonStr";
    public static String PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";
    public final static String PARAM_KEY_JSESSIONID = "JSESSIONID";

    static{
        if(BizConfig.RELEASE){
            DOMAIN = "mobile.manfenjiayuan.cn";
            URL_DEFAULT = "http://mobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue";
        }else{
            DOMAIN = "devmobile.manfenjiayuan.com";
            URL_DEFAULT = "http://devmobile.manfenjiayuan.cn/";
            PARAM_VALUE_QUEUE_NAME_DEF = "pmc-app-queue-test";
        }
//        URL_BASE_SERVER = NetFactory.getServerUrl();
    }

    //登录参数
    public final static String PARAM_KEY_USERNAME = "username";
    public final static String PARAM_KEY_PASSWORD = "password";
    public final static String PARAM_KEY_LOGIN_TYPE = "loginType";
    public final static String PARAM_KEY_LOGIN_KIND = "loginKind";
    public final static String PARAM_VALUE_LOGIN_TYPE_PMC = "PMC";

    public final static String URL_SESSION_LIST = "/biz/msg/getSessionList";

}
