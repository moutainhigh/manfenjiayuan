package com.mfh.litecashier.bean;

import java.io.Serializable;

/**
 * 订单明细
 * Created by bingshanguxue on 15/9/22.
 * <pre>
 *     {
         "productName": "9999999999995shangpin091502",
         "unitName": "",
         "thumbnail": null,
         "imgId": null,
         "skuId": 1252,
         "barcode": "9999999999995",
         "orderId": 126,
         "productId": 741,
         "bcount": 1,
         "amount": 5,
         "price": 5,
         "remark": null,
         "id": 5442,
         "createdBy": "132594",
         "createdDate": "2015-09-22 16:39:33",
         "updatedBy": "",
         "updatedDate": "2015-09-22 16:39:33"
     }
 * </pre>
 */
public class PosOrderItem implements Serializable {
    private Long orderId; //订单ID
//    private Long productId; //产品编号
//    private Long skuId; //商品sku编号
//    private Long bcount; //个数
//    private Double price; //单价
//    private Double amount; //总价
//    private String remark; //备注

    private String unitName;//单位名称
    private String barcode;//订单编号
    private String productName;
    private String imgUrl;//图片
    private Double bcount;
    private Double amount;
    private Double price;

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductName() {
        if (productName == null){
            return "";
        }
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
