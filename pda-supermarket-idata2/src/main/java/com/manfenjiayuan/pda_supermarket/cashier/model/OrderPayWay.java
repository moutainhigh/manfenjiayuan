package com.manfenjiayuan.pda_supermarket.cashier.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class OrderPayWay implements Serializable {
    private Long officeId;
    private Long tenantId;
    private Integer payType;
    private Double amount;
    private Date createdDate;
    private int bizType = 0;//0现金订单；1授信充值
    private String bizTypeCaption;

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getBizType() {
        return bizType;
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
    }

    public String getBizTypeCaption() {
        return bizTypeCaption;
    }

    public void setBizTypeCaption(String bizTypeCaption) {
        this.bizTypeCaption = bizTypeCaption;
    }
}
