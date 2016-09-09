package com.bingshanguxue.cashier.model.wrapper;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 7/10/16.
 */
public class PayWay implements Serializable{
    private Integer payType = WayType.NA;
    private Double amount = 0D;

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
