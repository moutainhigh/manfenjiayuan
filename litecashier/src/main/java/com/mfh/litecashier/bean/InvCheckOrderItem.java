package com.mfh.litecashier.bean;

import java.util.Date;

/**
 * 库存盘点订单明细
 * Created by Administrator on 2015/5/14.
 *
 */
public class InvCheckOrderItem implements java.io.Serializable{
//    {
//        "costPrice": 15,
//            "quantityInv": 9,
//            "quantityDif": -7,
//            "proSkuId": 4083,
//            "productName": "逗嘴泡椒凤爪",
//            "barcode": "6928497820014",
//            "quantityCheck": 2,
//            "netId": 132079,
//            "posId": 1,
//            "orderId": 29,
//            "orderType": 2,
//            "tenantId": 134221,
//            "id": 13880,
//            "createdBy": "132079",
//            "createdDate": "2015-11-06 11:45:28",
//            "updatedBy": "",
//            "updatedDate": "2015-11-29 14:59:56"
//    }


//    public static final int LOSS_STATUS_PROCESSING = 0;//生成，正在报损
//    public static final int LOSS_STATUS_FINISHED = 9;//已取消

    private Long id;//盘点单号
    private String productName;//商品名称
    private String barcode;//商品条码
    private String netId;//网点编号
    private String tenantId;//所属租户
    private String createdBy;//创建人id
    private Date createdDate;
    private Date updatedDate;
    private Double costPrice;//零售价
    private Double quantityInv;//库存数量
    private Double quantityCheck;//盘点数量

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getCostPrice() {
        if (costPrice == null){
            return 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getQuantityInv() {
        if (quantityInv == null){
            return 0D;
        }
        return quantityInv;
    }

    public void setQuantityInv(Double quantityInv) {
        this.quantityInv = quantityInv;
    }

    public Double getQuantityCheck() {
        if (quantityCheck == null){
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}
