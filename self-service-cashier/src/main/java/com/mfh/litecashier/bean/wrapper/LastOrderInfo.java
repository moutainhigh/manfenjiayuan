package com.mfh.litecashier.bean.wrapper;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 上一单信息
 *
 * Created by bingshanguxue on 8/2/16.
 */
public class LastOrderInfo implements Serializable {
    private int payType = WayType.NA;
    private Double finalAmount = 0D;
    private Double bCount = 0D;
    private Double discountAmount = 0D;
    private Double changeAmount = 0D;

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
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
