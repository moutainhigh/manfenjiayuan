package com.manfenjiayuan.mixicook_vip;

import com.mfh.framework.api.MfhApi;


/**
 * 支付宝 · APP支付
 * Created by bingshanguxue on 2015/5/16.
 *  重要说明:
 *
 *  这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
 *  真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
 *  防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险；
 */
public class AlipayConstants {

    /** 支付宝支付业务：入参app_id */
    public static final String APPID = "2016101502177977";
    /** 支付宝账户登录授权业务：入参pid值 */
    public static final String PID = "2088811788153062";//合作伙伴身份（PID），以2088开头的16位纯数字(鼎夏)
    /** 支付宝账户登录授权业务：入参target_id值 */
    public static final String TARGET_ID = "";
    /** 商户私钥，pkcs8格式 */
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOkpELoxXPsn/WyVeEQBSRoqRdUlopF7DN8G280J45Fe3R+VDAmxFwJMz3m/Kf7akH7n9gTTo/M5BbiS2ihoMJ0hZ/xyZDlh56JTfD3zrYfFI0W/WkWCzVC6bYR8T4Aikh0MTkHOpII27dy3adQQPfZVowYRY9b9JsJtsXx+/gLNAgMBAAECgYA731SxS03CHNHB2VwJsn7QHjzScBif3QZAGyXEMB7kzIRhwMdXkccDyy+X5tPk4sEqJWySbrpyEZl7fRJSP/cDVPyoUksZT1abXDanscw6AuiZM5fXiBn4/CbZkPChhOqNGuOI5gUJ9kTj36QQjSJ/n98rT3aE+7nyMskOoBVaIQJBAPf9aEaNvKZS8QbO66bRz2N+zLKRaxB6VHBO8BLv9K9t1wL1V4aANoXAsu2bK7HUGzXuYXdHP/4M3hqD9/FT7ykCQQDwsQkLZloDPrwtwErDc3ItPl6MSh8g/necKM5uyR+Dcp09QW5rKQ6oU0wRVZGMSqj+xnAkWB9GjV24LjYWrX8FAkAzwdG3l/FdjUX8s8b/BB8Slu5F/o+n2sAwyGjWqvoYewY6+dWQnsXCWJt/d7lA3hHnyZ7R9C7o6OqaAh0HYwsxAkAKLyN31QPYAU6LDQjczkS1f18WxogcqEe9VsitTVtE/5OBpQQcCqexHZ0pWpqG+h2+cf0KvKPOI2skwOfO+9ZVAkEAnjJYUSSUE/w+iSyS+prsl1p9M+lHoeWkJWyuJWTAH/SOOZrxFCI6BmZY6WvKlI66AeiXJ3sm5+TAuRkKP2CzaA==";

    public static final String CHARSET = "UTF-8";


    //商户收款账号
    public static final String SELLER = "finance@manfenjiayuan.com";

    //支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";


    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String ALIPAY_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";
    public static final String ALIPAY_ORDER_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/pmcstock/notifyOrder";

}
