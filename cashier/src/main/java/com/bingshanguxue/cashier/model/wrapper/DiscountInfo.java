package com.bingshanguxue.cashier.model.wrapper;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 促销规则和优惠券优惠
 * Created by bingshanguxue on 7/1/16.
 */
public class DiscountInfo implements Serializable {
    private Long orderId;//本地订单编号
    private int payType = WayType.RULES;//支付方式
    //折扣价(促销规则优惠)
    private Double ruleDiscountAmount = 0D;
    private String ruleIds = "";//促销规则编号,多个用逗号隔开
    //折扣价(卡券优惠)
    private Double couponDiscountAmount = 0D;
    private String couponsIds = "";//使用的优惠券编号,多个用逗号隔开
    //优惠后实际生效金额（会员+优惠券）
    private Double effectAmount = 0D;

    public DiscountInfo(Long orderId) {
        this.orderId = orderId;
    }


    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public Double getRuleDiscountAmount() {
        return ruleDiscountAmount;
    }

    public void setRuleDiscountAmount(Double ruleDiscountAmount) {
        this.ruleDiscountAmount = ruleDiscountAmount;
    }

    public Double getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(Double couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

    public Double getEffectAmount() {
        return effectAmount;
    }

    public void setEffectAmount(Double effectAmount) {
        this.effectAmount = effectAmount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }
}
