package com.bingshanguxue.cashier.model.wrapper;

import java.io.Serializable;

/**
 * 优惠券 & 规则
 * Created by Nat.ZZN(bingshanguxue) on 15/9/30.
 */
public class CouponRule implements Serializable {
    public static final Integer TYPE_COUPON = 0;//优惠券
    public static final Integer TYPE_RULE = 1;//规则
    private Integer type;//卡券类型，规则or优惠券

    private Long id;//编号

    private String title; //卡券标题
    private String subTitle; //副标题
    private Double discount = 0D;//折扣金额

    private Integer ruleExecType;//规则执行类型
    private Long couponsId;//卡券领用编号,不是卡券编号本身

    private boolean isSelected;//是否被选中
//    private List<Long> splitOrderIds;//关联的拆分订单编号,支持关联过个拆分订单，多个订单用逗号分隔
//    private Long splitOrderId;//当前指定关联的拆分订单编号
//    private Double amount;//关联的拆分订单金额

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public void toggleSelected(){
        this.isSelected = !this.isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

//    public List<Long> getSplitOrderIds() {
//        return splitOrderIds;
//    }
//
//    public void setSplitOrderIds(List<Long> splitOrderIds) {
//        this.splitOrderIds = splitOrderIds;
//    }
//
//    public Long getSplitOrderId() {
//        return splitOrderId;
//    }
//
//    public void setSplitOrderId(Long splitOrderId) {
//        this.splitOrderId = splitOrderId;
//    }

//    public Double getAmount() {
//        if (amount == null){
//            return 0D;
//        }
//        return amount;
//    }
//
//    public void setAmount(Double amount) {
//        this.amount = amount;
//    }
}
