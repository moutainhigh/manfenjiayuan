package com.bingshanguxue.cashier.model.wrapper;


import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;

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
    private Double salesBalance = 0D;//销售差价合计
    private Double turnOver = 0D;//营业额合计

    private List<AccItem> accItems;//经营流水分析数据
    private Double accBcount1 = 0D;
    private Double accAmount1 = 0D;

    private List<AccItem> accItems2;//充值流水分析数据
    private Double accBcount2 = 0D;
    private Double accAmount2 = 0D;


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


    public Double getSalesBalance() {
        return salesBalance;
    }

    public void setSalesBalance(Double salesBalance) {
        this.salesBalance = salesBalance;
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


    public Double getAccBcount1() {
        return accBcount1;
    }

    public void setAccBcount1(Double accBcount1) {
        this.accBcount1 = accBcount1;
    }

    public Double getAccAmount1() {
        return accAmount1;
    }

    public void setAccAmount1(Double accAmount1) {
        this.accAmount1 = accAmount1;
    }

    public List<AccItem> getAccItems() {
        return accItems;
    }

    public void setAccItems(List<AccItem> accItems) {
        this.accItems = accItems;
    }

    public List<AccItem> getAccItems2() {
        return accItems2;
    }

    public void setAccItems2(List<AccItem> accItems2) {
        this.accItems2 = accItems2;
    }

    public Double getAccBcount2() {
        return accBcount2;
    }

    public void setAccBcount2(Double accBcount2) {
        this.accBcount2 = accBcount2;
    }

    public Double getAccAmount2() {
        return accAmount2;
    }

    public void setAccAmount2(Double accAmount2) {
        this.accAmount2 = accAmount2;
    }
}
