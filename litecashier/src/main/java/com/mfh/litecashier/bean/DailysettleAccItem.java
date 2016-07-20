package com.mfh.litecashier.bean;


/**
 * 日结流水分析数据
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class DailysettleAccItem implements java.io.Serializable{
//    {
//        "status":1,
//            "aggDate":"2016-02-18 00:00:00",
//            "startDate":null,
//            "endDate":null,
//            "officeId":132079,
//            "payType":1,
//            "amount":15.0,
//            "orderNum":3,
//            "tenantId":134221,
//            "id":18,
//            "createdBy":"132079",
//            "createdDate":"2016-02-18 14:12:39",
//            "updatedBy":"",
//            "updatedDate":null
//    }

    private Integer payType;//支付类型
    private Double orderNum;//数量
    private Double amount;//金额

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
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
