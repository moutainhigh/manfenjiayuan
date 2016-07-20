package com.mfh.owner.utils;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.owner.alipay.SignUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 支付宝 · APP支付
 * Created by Administrator on 2015/5/16.
 */
public class AliPayUtil {
    //商户PID，以2088开头的16位纯数字
    public static final String PARTNER = "2088011585033309";

    //商户收款账号
    public static final String SELLER = "finance@manfenjiayuan.com";

    //商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOkpELoxXPsn/WyVeEQBSRoqRdUlopF7DN8G280J45Fe3R+VDAmxFwJMz3m/Kf7akH7n9gTTo/M5BbiS2ihoMJ0hZ/xyZDlh56JTfD3zrYfFI0W/WkWCzVC6bYR8T4Aikh0MTkHOpII27dy3adQQPfZVowYRY9b9JsJtsXx+/gLNAgMBAAECgYA731SxS03CHNHB2VwJsn7QHjzScBif3QZAGyXEMB7kzIRhwMdXkccDyy+X5tPk4sEqJWySbrpyEZl7fRJSP/cDVPyoUksZT1abXDanscw6AuiZM5fXiBn4/CbZkPChhOqNGuOI5gUJ9kTj36QQjSJ/n98rT3aE+7nyMskOoBVaIQJBAPf9aEaNvKZS8QbO66bRz2N+zLKRaxB6VHBO8BLv9K9t1wL1V4aANoXAsu2bK7HUGzXuYXdHP/4M3hqD9/FT7ykCQQDwsQkLZloDPrwtwErDc3ItPl6MSh8g/necKM5uyR+Dcp09QW5rKQ6oU0wRVZGMSqj+xnAkWB9GjV24LjYWrX8FAkAzwdG3l/FdjUX8s8b/BB8Slu5F/o+n2sAwyGjWqvoYewY6+dWQnsXCWJt/d7lA3hHnyZ7R9C7o6OqaAh0HYwsxAkAKLyN31QPYAU6LDQjczkS1f18WxogcqEe9VsitTVtE/5OBpQQcCqexHZ0pWpqG+h2+cf0KvKPOI2skwOfO+9ZVAkEAnjJYUSSUE/w+iSyS+prsl1p9M+lHoeWkJWyuJWTAH/SOOZrxFCI6BmZY6WvKlI66AeiXJ3sm5+TAuRkKP2CzaA==";

    //支付宝公钥
    public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

    /**
     * create the order info. 创建订单信息
     * @param totalFee 该笔订单的资金总额，单位为 RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位。
     * @param outTradeNo 商户网站唯一订单号
     * @param notifyUrl 服务器异步通知页面路径
     * @param token 满分家园支付验证码
     */
    public static String getOrderInfo(String subject, String body, String totalFee,
                                      String outTradeNo, String notifyUrl,
                                      String token) {
        // 签约合作者身份ID（不可空）
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号（不可空）
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";
//        orderInfo += "&seller=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号（不可空）
        orderInfo += "&out_trade_no=" + "\"" + outTradeNo + "\"";

        // 商品名称（不可空）
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情（不可空）
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额（不可空）
        orderInfo += "&total_fee=" + "\"" + totalFee + "\"";

        // 服务器异步通知页面路径（不可空）
        orderInfo += "&notify_url=" + "\"" + notifyUrl
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";


        // 支付验证码（满分家园）
        if(StringUtils.isEmpty(token)){
            orderInfo += "&token=" + "\"" + token + "\"";
        }

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     *
     */
    public static String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content
     *            待签名订单信息
     */
    public static String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
    public static String getSignType() {
        return "sign_type=\"RSA\"";
    }

    /**
     * 生成支付信息
     * @param notifyUrl 服务器异步通知页面路径
     * @param token 满分家园支付验证码
     * */
    public static String genPayInfo(String subject, String body, String totalFee,
                                    String outTradeNo, String notifyUrl, String token){
        // 订单
        String orderInfo = AliPayUtil.getOrderInfo(subject, body, totalFee, outTradeNo,
                notifyUrl, token);

        // 对订单做RSA 签名
        String sign = AliPayUtil.sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + AliPayUtil.getSignType();
        ZLogger.d("genPayInfo:" + payInfo);

        return payInfo;
    }

}
