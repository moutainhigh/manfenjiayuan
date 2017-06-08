package com.mfh.framework.api.analysis;


/**
 * 交接班/日结经营分析数据
 * Created by bingshanguxue on 2015/8/18.
 */
public class AggItem implements java.io.Serializable {
    private Integer bizType;//业务类型
    private String bizTypeCaption;
    private Integer subType;//子类型
    private String subTypeCaption;
    private Double orderNum = 0D;//数量
    private Double turnover = 0D;//营业额
    private Double origionAmount = 0D;//原价金额
    private Double salesBalance = 0D;//销售差额 = 营业额 - 采购成本

    private Double grossProfit = 0D;//毛利

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Double getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Double orderNum) {
        this.orderNum = orderNum;
    }

    public Double getTurnover() {
        if (turnover == null) {
            return 0D;
        }
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    public Double getSalesBalance() {
        if (salesBalance == null) {
            return 0D;
        }
        return salesBalance;
    }

    public void setSalesBalance(Double salesBalance) {
        this.salesBalance = salesBalance;
    }

    public Double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public String getBizTypeCaption() {
        return bizTypeCaption;
    }

    public void setBizTypeCaption(String bizTypeCaption) {
        this.bizTypeCaption = bizTypeCaption;
    }

    public String getSubTypeCaption() {
        return subTypeCaption;
    }

    public void setSubTypeCaption(String subTypeCaption) {
        this.subTypeCaption = subTypeCaption;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }


    public Double getOrigionAmount() {
        return origionAmount;
    }

    public void setOrigionAmount(Double origionAmount) {
        this.origionAmount = origionAmount;
    }
}
