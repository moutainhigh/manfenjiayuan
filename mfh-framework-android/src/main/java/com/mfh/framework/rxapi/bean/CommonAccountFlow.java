package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

import java.util.Date;

/**
 * 会员账户消费流水
 * Created by bingshanguxue on 05/07/2017.
 */

public class CommonAccountFlow extends MfhEntity<Long> {
    private Double curCash;
    private Double conCash;
    private String remark;
    private Integer bizType;
    private Integer wayType;
    private Date happenDate;
    private String tradeNo;

    public Double getCurCash() {
        return curCash;
    }

    public void setCurCash(Double curCash) {
        this.curCash = curCash;
    }

    public Double getConCash() {
        if (conCash == null) {
            return 0D;
        }
        return conCash;
    }

    public void setConCash(Double conCash) {
        this.conCash = conCash;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Integer getWayType() {
        return wayType;
    }

    public void setWayType(Integer wayType) {
        this.wayType = wayType;
    }

    public Date getHappenDate() {
        return happenDate;
    }

    public void setHappenDate(Date happenDate) {
        this.happenDate = happenDate;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
}
