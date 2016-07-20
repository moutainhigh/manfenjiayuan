package com.bingshanguxue.cashier.model;

import java.io.Serializable;

/**
 * 支付金额
 * 适用场景：会员支付获取优惠券
 * Created by bingshanguxue on 6/29/16.
 */
public class PayAmount implements Serializable{
    private Double payAmount; // 优惠后总支付金额
    private Double transFee; // 其中：物流费用
    private Double ruleAmount;//促销规则优惠金额
    private Double coupAmount;//卡券优惠金额

    public Double getPayAmount() {
        if (payAmount == null){
            return 0D;
        }
        return payAmount;
    }

    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }

    public Double getTransFee() {
        if (transFee == null){
            return 0D;
        }
        return transFee;
    }

    public void setTransFee(Double transFee) {
        this.transFee = transFee;
    }

    public Double getRuleAmount() {
        if (ruleAmount == null){
            return 0D;
        }
        return ruleAmount;
    }

    public void setRuleAmount(Double ruleAmount) {
        this.ruleAmount = ruleAmount;
    }

    public Double getCoupAmount() {
        if (coupAmount == null){
            return 0D;
        }
        return coupAmount;
    }

    public void setCoupAmount(Double coupAmount) {
        this.coupAmount = coupAmount;
    }
}
