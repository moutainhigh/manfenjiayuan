package com.mfh.litecashier.bean;


/**
 * 交接班数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class HandoverAggItem implements java.io.Serializable{
//    {
//        "shiftId":1,
//            "aggDate":"2016-02-02 00:00:00",
//            "startDate":"2016-02-02 07:00:00",
//            "endDate":"2016-02-02 19:00:00",
//            "officeId":134337,
//            "bizType":2,
//            "turnover":1.2,
//            "orderNum":2,
//            "tenantId":134342,
//            "id":23,
//            "createdBy":"134475",
//            "createdDate":"2016-02-02 22:29:21",
//            "updatedBy":"",
//            "updatedDate":null
//    }

    private Integer shiftId;//班次
    private Integer bizType;//业务类型
    private Double orderNum;//数量
    private Double turnover;//金额

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

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
}
