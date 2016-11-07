package com.mfh.framework.api.pay;

/**
 * 预支付订单响应返回数据
 * Created by binshanguxue on 2015/5/14.
 *
 */
public class PreOrderRsp implements java.io.Serializable{
    private String amount;//订单金额
    private Long id;//订单编号
    private String token;//支付验证码
    private String nonceStr;//随机字符串

    //ALIPAY
    private String payInfo;//签名后的请求串
    //WEPAY
    private String prepayId;
    private String sign;//签名

    public PreOrderRsp(){
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getPayInfo() {
        return payInfo;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }
}