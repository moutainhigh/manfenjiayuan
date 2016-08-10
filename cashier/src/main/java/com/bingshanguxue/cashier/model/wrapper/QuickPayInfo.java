package com.bingshanguxue.cashier.model.wrapper;

import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 快捷支付
 * Created by bingshanguxue on 8/8/16.
 */
public class QuickPayInfo implements Serializable{
    private Integer bizType;//业务类型
    private String subject;//主题
    private String body;//内容
    private Integer payType = WayType.NA;
    private Double amount;//支付金额
    private Double minAmount;

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }
}
