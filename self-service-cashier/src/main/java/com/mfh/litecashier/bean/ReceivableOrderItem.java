package com.mfh.litecashier.bean;


import java.io.Serializable;

/**
 *  接收商品订单
 * Created by kun on 15/9/22.
 */
public class ReceivableOrderItem implements Serializable {

//        {
// "imgUrl": "http://chunchunimage.b0.upaiyun.com/product/3654.JPG!small",
//            "orderId": "117",
//                "proSkuId": "1619",
//                "chainSkuId": null,
//                "productName": "可口可乐 1.25L",
//                "unitSpec": null,
//                "singleCount": null,
//                "packCount": null,
//                "totalCount": 1,
//                "price": 7.5,
//                "amount": 7.5,
//                "receiveCount": null,
//                "receiveAmount": null,
//                "barcode": "6901939671603",
//                "tenantId": "130222",
//                "id": "3109",
//                "createdBy": "",
//                "createdDate": "2015-12-16 17:29:06",
//                "updatedBy": "",
//                "updatedDate": null
//        }

    private String orderId;//订单编号
    private String id;
    private String productName;//商品名称
    private Double totalCount;//配送数量
    private Double price;//价格
    private Double amount;//总价
    private String unitSpec;//单位
    private String barcode;//条码
    private String imgUrl;//图片

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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

    public Double getTotalCount() {
        if (totalCount == null){
            return 0D;
        }
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
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

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnitSpec() {
        if (unitSpec == null){
            return "";
        }
        return unitSpec;
    }

    public void setUnitSpec(String unitSpec) {
        this.unitSpec = unitSpec;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

}
