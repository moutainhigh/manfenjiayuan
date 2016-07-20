package com.mfh.framework.pay.alipay;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * 支付宝支付
 *
 * Created by bingshanguxue on 4/20/16.
 */
public class AlipayUtil {


    /**
     * create the order info. 创建订单信息
     * @param totalFee 该笔订单的资金总额，单位为 RMB-Yuan。取值范围为[0.01，100000000.00]，精确到小数点后两位。
     * @param outTradeNo 商户网站唯一订单号
     * @param notifyUrl 服务器异步通知页面路径
     * @param token 满分家园支付验证码
     */
    private static String getOrderInfo(String parter, String seller, String subject, String body, String totalFee,
                                      String outTradeNo, String notifyUrl,
                                      String token) {
        // 签约合作者身份ID（不可空）
        String orderInfo = "partner=" + "\"" + parter + "\"";

        // 签约卖家支付宝账号（不可空）
        orderInfo += "&seller_id=" + "\"" + seller + "\"";
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
        orderInfo += "&_input_charset=\"" + SignUtils.DEFAULT_CHARSET + "\"";

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
    private static String getOutTradeNo() {
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
     * get the sign type we use. 获取签名方式
     *
     */
    private static String getSignType() {
        return "sign_type=\"" + SignUtils.ALGORITHM + "\"";
    }

    /**
     * 生成支付信息
     * @param notifyUrl 服务器异步通知页面路径
     * @param token 满分家园支付验证码
     * */
    public static String genPayInfo(String parter, String seller, String privateKey, String subject, String body, String totalFee,
                                    String outTradeNo, String notifyUrl, String token){
        // 订单
        String orderInfo = getOrderInfo(parter, seller, subject, body, totalFee, outTradeNo,
                notifyUrl, token);

        // 对订单做RSA 签名
        String sign = SignUtils.sign(orderInfo, privateKey);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();
        ZLogger.d("genPayInfo:" + payInfo);

        return payInfo;
    }
}
