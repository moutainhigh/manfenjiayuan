package com.manfenjiayuan.pda_supermarket.cashier.model.wrapper;

import java.io.Serializable;
import java.util.Date;

/**
 * 挂单
 * Created by bingshanguxue on 6/22/16.
 */
public class HangupOrder implements Serializable{
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