package com.mfh.litecashier.ui.fragment.dailysettle;

import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 日结
 * Created by bingshanguxue on 16/12/2016.
 */

public class DailysettleInfo implements Serializable{
    private Long officeId;
    private String officeName;
    private String humanName;

    private Date createdDate;
    private Date updatedDate;

    private List<AggItem> aggItems;//经营分析数据
    private Double grossProfit = 0D;//毛利
    private Double turnOver = 0D;//营业额合计

    private List<AccItem> accItems;//流水分析数据
    private Double cash = 0D;//现金

    public Long getOfficeId() {
        return officeId;
    }

    public void setOfficeId(Long officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Double getTurnOver() {
        return turnOver;
    }

    public void setTurnOver(Double turnOver) {
        this.turnOver = turnOver;
    }

    public List<AggItem> getAggItems() {
        return aggItems;
    }

    public void setAggItems(List<AggItem> aggItems) {
        this.aggItems = aggItems;
    }


    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    public List<AccItem> getAccItems() {
        return accItems;
    }

    public void setAccItems(List<AccItem> accItems) {
        this.accItems = accItems;
    }
}
