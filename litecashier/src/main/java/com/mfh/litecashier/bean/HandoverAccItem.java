package com.mfh.litecashier.bean;


/**
 * 交接班数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class HandoverAccItem implements java.io.Serializable{
//    {
//        "shiftId":4,
//            "aggDate":"2016-02-04 00:00:00",
//            "startDate":"2016-02-04 17:21:16",
//            "endDate":"2016-02-04 17:57:30",
//            "officeId":132079,
//            "payType":1,
//            "amount":1.0,
//            "orderNum":2,
//            "tenantId":134221,
//            "id":24,
//            "createdBy":"132079",
//            "createdDate":"2016-02-03 17:55:49",
//            "updatedBy":"",
//            "updatedDate":null
//    }

    private Integer shiftId;//班次
    private Integer payType;//支付方式
    private Double orderNum;//数量
    private Double amount;//金额

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }


    public Double getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Double orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
