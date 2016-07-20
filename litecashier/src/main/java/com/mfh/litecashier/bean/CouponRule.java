package com.mfh.litecashier.bean;

import java.io.Serializable;

/**
 * 优惠券 & 规则
 * Created by Nat.ZZN(bingshanguxue) on 15/9/30.
 */
public class CouponRule implements Serializable {

    public static final int TYPE_COUPON = 0;//优惠券
    public static final int TYPE_RULE = 1;//规则

    private String title; //卡券标题
    private String subTitle; //副标题
    private Double discount = 0D;//折扣金额
    private Long couponsId;//我领用的卡券编号,不是卡券编号本身

    private Integer type;//卡券类型，规则or优惠券
    private Integer ruleExecType;//规则执行类型
    private boolean isSelected;//是否被选中

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Double getDiscount() {
        if (discount == null){
            return 0D;
        }
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Long getCouponsId() {
        return couponsId;
    }

    public void setCouponsId(Long couponsId) {
        this.couponsId = couponsId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getRuleExecType() {
        return ruleExecType;
    }

    public void setRuleExecType(Integer ruleExecType) {
        this.ruleExecType = ruleExecType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void toggleSelected(){
        this.isSelected = !this.isSelected;
    }
}
