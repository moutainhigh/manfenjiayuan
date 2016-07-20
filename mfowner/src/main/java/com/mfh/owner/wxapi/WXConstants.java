package com.mfh.owner.wxapi;

import com.mfh.framework.api.MfhApi;

/**
 * 微信配置参数
 * Created by Administrator on 2014/11/15.
 */
public class WXConstants {
    //微信开放平台(open.weixin.qq.com)
    //APPID 微信开放平台APP的唯一标识
    //请同时修改  androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
    public static final String APP_ID = "wx1dbac2f50c918d7d";
    //Appsecret APPID对应的接口密码
    public static final String APP_SECRET = "25f4df09a0790cabc02ae96fd32af616";

    // 微信支付商户号
    public static final String MCH_ID = "1250378401";
    // API密钥，在商户平台设置
    public static final  String API_KEY= "manfenjiayuanchunchenwangluo2015";

    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    public static final String CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth";
    public static final String GET_USER_INFO = "https://api.weixin.qq.com/sns/userinfo";
    public static final String AUTH_SUCCESS = "weixin.auth.success";
    public static final String AUTH_FAIL = "weixin.auth.fail";
    public static final String AUTH_START = "weixin.auth.start";
    public static final String BIND_TO_XEIXIN = "bind.to.weixin";
    public static final String LOGIN_SUCCESS = "login_success";


    //微信APP支付统一下单URL地址
    public static final String URL_UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";


    public static final String ACTION_WXPAY_RESP = "ACTION_WXPAY_RESP";//微信支付处理结果
    public static final String EXTRA_KEY_ERR_CODE = "EXTRA_KEY_ERR_CODE";
    public static final String EXTRA_KEY_ERR_STR = "EXTRA_KEY_ERR_STR";

}
