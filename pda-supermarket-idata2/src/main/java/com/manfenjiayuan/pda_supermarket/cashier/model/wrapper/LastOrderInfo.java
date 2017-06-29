package com.manfenjiayuan.pda_supermarket.cashier.model.wrapper;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 上一单信息
 *
 * Created by bingshanguxue on 8/2/16.
 */
public class LastOrderInfo implements Serializable {
    private Long orderId;//本地订单编号
    private Double finalAmount = 0D;
    private Double bCount = 0D;
    private int payType = WayType.NA;
    private Double discountAmount = 0D;
    private Double changeAmount = 0D;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
    }


    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Double changeAmount) {
        this.changeAmount = changeAmount;
    }

}
