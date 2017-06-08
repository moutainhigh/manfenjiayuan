package com.mfh.framework.api.posorder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bingshanguxue on 24/04/2017.
 */

public class BatchInOrder implements Serializable {
    private Long id;
    private String barCode;
    private Integer status;
    private String remark;
    private Double bcount;
    private Double adjPrice;
    private Integer paystatus;
    private Integer subType;
    private String outerNo;
    private String posId;
    private Long sellOffice;
    private Long sellerId;
    private Long humanId;
    private String createdDate;
    private String createdBy;
    private List<BatchInOrderItem> items;
    private Object rpDisAmountMap;
    private List<PayWay> payWays;
    private Double disAmount;
    private String couponsIds;
    private String ruleIds;
    private Integer payType;
    private Double amount;
    private Double score;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getAdjPrice() {
        return adjPrice;
    }

    public void setAdjPrice(Double adjPrice) {
        this.adjPrice = adjPrice;
    }

    public Integer getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(Integer paystatus) {
        this.paystatus = paystatus;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getOuterNo() {
        return outerNo;
    }

    public void setOuterNo(String outerNo) {
        this.outerNo = outerNo;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public Long getSellOffice() {
        return sellOffice;
    }

    public void setSellOffice(Long sellOffice) {
        this.sellOffice = sellOffice;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<BatchInOrderItem> getItems() {
        return items;
    }

    public void setItems(List<BatchInOrderItem> items) {
        this.items = items;
    }

    public Object getRpDisAmountMap() {
        return rpDisAmountMap;
    }

    public void setRpDisAmountMap(Object rpDisAmountMap) {
        this.rpDisAmountMap = rpDisAmountMap;
    }

    public List<PayWay> getPayWays() {
        return payWays;
    }

    public void setPayWays(List<PayWay> payWays) {
        this.payWays = payWays;
    }

    public Double getDisAmount() {
        return disAmount;
    }

    public void setDisAmount(Double disAmount) {
        this.disAmount = disAmount;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
