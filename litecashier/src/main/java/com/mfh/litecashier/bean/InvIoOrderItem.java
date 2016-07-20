package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;

/**
 *  批次流水明细
 * Created by kun on 15/9/22.
 */
public class InvIoOrderItem implements ILongId, Serializable {

//    {
//        "singleCount": null,
//            "quantityPack": null,
//            "price": 0,
//            "tenantSkuId": null,
//            "proSkuId": 19374,
//            "productName": "水天堂“福月”",
//            "barcode": "1111",
//            "quantityCheck": 1,
//            "netId": 132079,
//            "posId": null,
//            "orderId": 1253,
//            "orderType": 1,
//            "tenantId": 134221,
//            "id": 104592,
//            "createdBy": "",
//            "createdDate": "2016-01-20 23:32:53",
//            "updatedBy": "",
//            "updatedDate": "2016-01-20 23:32:53"
//    }

    private Long id;//订单编号
    private String productName;//商品名称
    private String barcode;//条码
    private Double price;//配销价
    private Double quantityCheck;//数量
    private String orderTypeCaption;//订单类型

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Double getPrice() {
        if (price == null){
            return 0D;
        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public String getOrderTypeCaption() {
        return orderTypeCaption;
    }

    public void setOrderTypeCaption(String orderTypeCaption) {
        this.orderTypeCaption = orderTypeCaption;
    }
}
