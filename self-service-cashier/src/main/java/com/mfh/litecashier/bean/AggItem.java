package com.mfh.litecashier.bean;


/**
 * 交接班/日结经营分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AggItem implements java.io.Serializable{
    private Integer bizType;//业务类型
    private String bizTypeCaption;
    private Integer subType;//子类型
    private String subTypeCaption;
    private Double orderNum = 0D;//数量
    private Double turnover = 0D;//金额

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
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
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
}
