package com.mfh.litecashier.bean.wrapper;

import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.utils.AnalysisHelper;

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
    private Double amount;//营业额合计
    private Double income;//账户新增
    private Double cash;//现金收取

    //按业务类型
    private AggWrapper aggWrapper = new AggWrapper();


    //按支付类型
    private AccWrapper accWrapper = new AccWrapper();

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

    public AggWrapper getAggWrapper() {
        return aggWrapper;
    }

    public void setAggWrapper(AggWrapper aggWrapper) {
        this.aggWrapper = aggWrapper;
    }


    public AccWrapper getAccWrapper() {
        return accWrapper;
    }

    public void setAccWrapper(AccWrapper accWrapper) {
        this.accWrapper = accWrapper;
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

    public Double getAmount() {
        if (amount == null){
            amount = 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getIncome() {
        if (income == null){
            return 0D;
        }
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
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
        this.aggWrapper = new AggWrapper(aggItems);
        this.amount = this.aggWrapper.getTurnOver();
    }

    public List<AnalysisItemWrapper> getAggAnalysisList(){
        if (this.aggWrapper == null){
            this.aggWrapper = new AggWrapper();
        }

        return AnalysisHelper.getAggAnalysisList(this.aggWrapper);

    }

    /**
     * 设置交接明细（按支付类型）
     * */
    public void setAccItems(List<AccItem> accItems) {
        if (this.accWrapper == null){
            this.accWrapper = new AccWrapper();
        }
        this.accWrapper.initialize(accItems);
        AccItem cashItem = this.accWrapper.getCash();
        this.cash = cashItem != null ? cashItem.getAmount() : 0D;
        this.income = this.amount - this.cash;
    }

    public List<AnalysisItemWrapper> getAccAnalysisList(){
        if (this.accWrapper == null){
            this.accWrapper = new AccWrapper();
        }

        return AnalysisHelper.getAccAnalysisList(this.accWrapper);
    }
}
