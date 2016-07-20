package com.mfh.litecashier.bean.wrapper;

import java.util.Date;

/**
 * Created by bingshanguxue on 6/22/16.
 */
public class HangupOrder {
    private String orderTradeNo;
    private Date updateDate;
    private Double finalAmount;

    public String getOrderTradeNo() {
        return orderTradeNo;
    }

    public void setOrderTradeNo(String orderTradeNo) {
        this.orderTradeNo = orderTradeNo;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Double getFinalAmount() {
        if (finalAmount == null){
            return 0D;
        }
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }
}
