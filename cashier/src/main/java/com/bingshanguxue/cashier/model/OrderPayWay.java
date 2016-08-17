package com.bingshanguxue.cashier.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class OrderPayWay implements Serializable{
    private Long officeId;
    private Long tenantId;
    private Integer payType;
    private Double amount;
    private Date createdDate;
//    private String

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
}
