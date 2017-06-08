package com.mfh.litecashier.bean.wrapper;


/**
 * 交接班/日结
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AnalysisItemWrapper implements java.io.Serializable{
    private String caption;//业务类型/支付类型
    private Double orderNum = 0D;//订单数量
    private Double turnover = 0D;//金额
    private Double grossProfit = 0D;//毛利
    private Double origionAmount = 0D;//原价金额
    private Double salesBalance = 0D;//销售差额 = 营业额 - 采购成本


    private boolean isShowIndex;//是否显示序号

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Double getOrderNum() {
        if (orderNum == null){
            return 0D;
        }
        return orderNum;
    }

    public void setOrderNum(Double orderNum) {
        this.orderNum = orderNum;
    }

    public Double getTurnover() {
        if (turnover == null){
            return 0D;
        }
        return turnover;
    }

    public void setTurnover(Double turnover) {
        this.turnover = turnover;
    }

    public boolean isShowIndex() {
        return isShowIndex;
    }

    public void setIsShowIndex(boolean isShowIndex) {
        this.isShowIndex = isShowIndex;
    }

    public Double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Double getOrigionAmount() {
        if (origionAmount == null){
            return 0D;
        }
        return origionAmount;
    }

    public void setOrigionAmount(Double origionAmount) {
        this.origionAmount = origionAmount;
    }

    public Double getSalesBalance() {
        if (salesBalance == null){
            return 0D;
        }
        return salesBalance;
    }

    public void setSalesBalance(Double salesBalance) {
        this.salesBalance = salesBalance;
    }
}
