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
    public static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALN7fuDnxdXo5C+zubcHXdg1vjJFdLIACQrFE41zaD/tS0dSeGUPJUM2dbAfdSjbQkLKX/btGV1oLgXgJVLBoCZN79R7cPoeBNVusx/04V59omIn1QfzfrUGwNT+hJbII1K8uudzwbmWD7VBztu63CXPG1FOFtaPMasDhxFT8zVLAgMBAAECgYEAiGXoxWi+K1Mi3rGaCGNbyS0GjtPD3GY0eO/ObSfwaOyf0eL1xe9dpRelFqTBd7wxPeroRRJovVin6SUvmBW36z0hfm52mk/7kG49JI4da+g6DbT4PlSy/vWvfOCpo6U6D0r26xxbuubM1xMANWKCQY9OQ+g8/yVZzoKxJ5rQSsECQQDqbt19+DK4EgAkDwrw3k9V/uNo0fJNsr9JNNJISTTJfu8LVHRmiKUiZc4QzCpfjx+n30RTtqtoZYrq92Fyzx9hAkEAw/58quLxTACTVeO6Kf3rRiPPwh89fQokDHmkbq4LnJzZ+G8+YAJmFAI9lg6m1h/GnpEYyDmHY00UXnJy7H7wKwJBANW8ZHZgHqXRy40Upe37Uave6qj7mQWb7WiqHvpGvDjlYL4crs94z83vsZOBATUEgWEFXv9P8TEKO0CEcLVC/6ECQBEQdTZDsBKX8jwGKc1CKFvg/lO+eJIMcfsMFD72tFgcZ/XzaZDeZJjVYRtRh0EZXyerJmc4awtASMe4Wz0wCCUCQQClaC8M0WTIQ3vclmPPhlVh025E464K8SnSoSKJLL9qoPpz/+vnTy2qvDthIAJxwnsgLF4u8dArqTTIMko5kymO";

    public static final String CHARSET = "UTF-8";

    /**商户收款账号*/
    public static final String SELLER = "755943995@qq.com";

    //支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";


    //服务器异步通知页面路径//"http://notify.msp.hk/notify.htm"
    public static final String ALIPAY_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/commonuseraccount/notifyAccount";
    public static final String ALIPAY_ORDER_NOTIFY_URL = MfhApi.URL_BASE_SERVER + "/pmcstock/notifyOrder";

}
