package com.mfh.enjoycity.bean;

/**
 * 预支付 · 支付宝
 * Created by ZZN.NAT on 2015/5/14.
 *
 */
public class PreOrderResponse implements java.io.Serializable{
    private String amount;
    private String id;//订单号
    private String token;//支付验证码

    //WEPAY
    private String prepayId;
    private String sign;//签名

    public PreOrderResponse(){
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\namount=%s", amount));
        sb.append(String.format("\nid=%s", id));
//        sb.append(subject);
//        sb.append(body);
        sb.append(String.format("\ntoken=%s", token));
        sb.append(String.format("\nprepayId=%s", prepayId));
        sb.append(String.format("\nsign=%s", sign));
        return sb.toString();
    }
}