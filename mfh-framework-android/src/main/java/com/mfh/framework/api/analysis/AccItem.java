package com.mfh.framework.api.analysis;


/**
 * 交接班／日结流水分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class AccItem implements java.io.Serializable{
    private Integer payType;//支付类型
    private String payTypeCaption;
    private Double orderNum = 0D;//数量
    private Double amount = 0D;//金额

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getPayTypeCaption() {
        return payTypeCaption;
    }

    public void setPayTypeCaption(String payTypeCaption) {
        this.payTypeCaption = payTypeCaption;
    }

    public Double getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Double orderNum) {
        this.orderNum = orderNum;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
