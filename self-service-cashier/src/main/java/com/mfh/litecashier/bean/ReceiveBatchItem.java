package com.mfh.litecashier.bean;


import java.io.Serializable;
import java.util.Date;

/**
 *  交易订单
 * Created by kun on 15/9/22.
 */
public class ReceiveBatchItem implements Serializable {

    //    "bean": {
//        "stockId": 1217,
//                "subdisId": null,
//                "companyId": 130003,
//                "humanId": 132660,
//                "batchCount": 0,
//                "smsCount": 0,
//                "weixinCount": 0,
//                "receivedCount": 0,
//                "income": 0.0,
//                "totalCost": 0.0,
//                "stockType": 2,
//                "tenantId": 134221,
//                "id": 73720,
//                "createdBy": "132079",
//                "createdDate": "2015-12-15 16:14:51",
//                "updatedBy": "",
//                "updatedDate": "2015-12-15 16:14:52"
//    }
//    {"companyId":"申通快递","humanId":"李轩","stockId":"荣域花园好邻居店"}

    private Long id;//批次编号，batchId

    private Integer batchCount;//数量
    private Integer smsCount;//短信条数
    private Double income;//收入
    private Double totalCost;//支出
    private Date createdDate;//创建时间

    private String courierName;//快递员
    private String courierPhone;//快递员
    private String companyName;//快递公司
    private String stockName;//网点


    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getCourierPhone() {
        return courierPhone;
    }

    public void setCourierPhone(String courierPhone) {
        this.courierPhone = courierPhone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Integer getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(Integer batchCount) {
        this.batchCount = batchCount;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getSmsCount() {
        return smsCount;
    }

    public void setSmsCount(Integer smsCount) {
        this.smsCount = smsCount;
    }

    //    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
