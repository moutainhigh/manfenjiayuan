package com.mfh.buyers.bean;

/**
 * 预支付·微信
 * Created by NAT.ZZN on 2015/5/14.
 *
 */
public class WXPrePayResponse implements java.io.Serializable{
    private String prepayId;
    private String sign;//签名

    public WXPrePayResponse(){
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
        sb.append(String.format("\nprepayId=%s", prepayId));
        sb.append(String.format("\nsign=%s", sign));
        return sb.toString();
    }


}
