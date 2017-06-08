package com.mfh.framework.api.posorder;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 7/10/16.
 */
public class PayWay implements Serializable{
    /**支付类型，和订单支付类型保持一致*/
    private Integer payType = WayType.NA;
    private Integer amountType = 0;
    private Double amount = 0D;

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getAmountType() {
        return amountType;
    }

    public void setAmountType(Integer amountType) {
        this.amountType = amountType;
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
