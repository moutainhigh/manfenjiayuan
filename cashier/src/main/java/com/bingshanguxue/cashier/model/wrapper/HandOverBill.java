package com.bingshanguxue.cashier.model.wrapper;

import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;

import java.util.Date;
import java.util.List;

/**
 * 交接班单据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class HandOverBill implements java.io.Serializable{
    private String officeName;//门店名称
    private String humanName;//收银员姓名
    private int shiftId;// 班次
    private Date startDate;//上班时间
    private Date endDate;//交接时间

    private List<AggItem> aggItems;//经营分析数据
    private Double turnover;//营业额合计
    private Double origionAmount;//原价金额

    private List<AccItem> accItems;//流水分析数据
    private Double cash;//现金收取

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


    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public List<AggItem> getAggItems() {
        return aggItems;
    }

    public List<AccItem> getAccItems() {
        return accItems;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    public Double getTurnover() {
        if (turnover == null){
            turnover = 0D;
        }
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    public Double getOrigionAmount() {
        if (origionAmount == null){
            origionAmount = 0D;
        }
        return origionAmount;
    }

    public void setOrigionAmount(Double origionAmount) {
        this.origionAmount = origionAmount;
    }

    public Double getCash() {
        if (cash == null){
            return 0D;
        }
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }


    public void setAggItems(List<AggItem> aggItems) {
        this.aggItems = aggItems;
    }

    public void setAccItems(List<AccItem> accItems) {
        this.accItems = accItems;
    }
}
