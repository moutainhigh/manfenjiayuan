package com.mfh.litecashier.bean;


/**
 * 日结经营分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class DailysettleAggItem implements java.io.Serializable{
//    {
//        "aggDate":"2016-02-18 00:00:00",
//            "officeId":132079,
//            "bizType":7,
//            "tenantId":134221,
//            "turnover":7176.0,//金额
//            "grossProfit":7176.0,//毛利
//            "cost":0.0,
//            "netProfit":7176.0,//净利
//            "cashReceived":0.0,
//            "orderNum":11,//订单数
//            "goodsNum":11,
//            "guPrice":652.36,//平均客单价
//            "id":72015,
//            "createdBy":"",
//            "createdDate":null,
//            "updatedBy":"",
//            "updatedDate":null
//    }

    private Integer bizType;//业务类型
    private Double orderNum;//数量
    private Double turnover;//金额
    private Double grossProfit;//毛利


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
}
