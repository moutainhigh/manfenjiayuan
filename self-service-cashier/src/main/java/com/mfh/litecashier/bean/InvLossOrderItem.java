package com.mfh.litecashier.bean;


/**
 * 报损订单明细
 * Created by Administrator on 2015/5/14.
 *
 */
public class InvLossOrderItem implements java.io.Serializable{
//    {
//        "costPrice": 15.5,
//            "quantityInv": 1,
//            "proSkuId": 4089,
//            "productName": "鱿鱼圈",
//            "barcode": "6901248894007",
//            "quantityCheck": 1,
//            "netId": 132079,
//            "posId": 0,
//            "orderId": 30,
//            "orderType": 1,
//            "tenantId": 134221,
//            "id": 14561,
//            "createdBy": "132079",
//            "createdDate": "2015-11-25 20:31:26",
//            "updatedBy": "",
//            "updatedDate": "2015-11-29 15:00:10"
//    }

    public static final int LOSS_STATUS_PROCESSING = 0;
    public static final int LOSS_STATUS_FINISHED = 1;

    private String id;//盘点单号
    private String productName;//商品名称
    private String barcode;//商品条码
    private Double quantityCheck;//报损数量
    private Double price;//报损价格

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Double getQuantityCheck() {
        if (quantityCheck == null){
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public Double getPrice() {
        if (price == null){
            return 0D;
        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
