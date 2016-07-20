package com.mfh.enjoycity.wxapi;

import com.mfh.framework.api.MfhApi;

/**
 * 微信配置参数
 * Created by Administrator on 2014/11/15.
 */
public class WXConstants {
    //微信开放平台(open.weixin.qq.com)
    //APPID 微信开放平台APP的唯一标识
    //请同时修改  androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
    public static final String APP_ID = "wx0d409f556a21b0ea";
    //Appsecret APPID对应的接口密码
    public static final String APP_SECRET = "fed9638da3bfcb444f4347af841ee468";

    // 微信支付商户号
    public static final String MCH_ID = "1264081801";
    // API密钥，在商户平台设置
    public static final  String API_KEY= "manfenjiayuanchunchenwangluo2017";

    //微信APP支付统一下单URL地址
    public static final String URL_UNIFIED_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";


    public static final String ACTION_WXPAY_RESP = "ACTION_WXPAY_RESP";//微信支付处理结果
    public static final String EXTRA_KEY_ERR_CODE = "EXTRA_KEY_ERR_CODE";
    public static final String EXTRA_KEY_ERR_STR = "EXTRA_KEY_ERR_STR";

}
