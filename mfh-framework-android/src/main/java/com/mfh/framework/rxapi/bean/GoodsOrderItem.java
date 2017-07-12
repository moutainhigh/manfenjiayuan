package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

import me.drakeet.multitype.Item;

/**
 * 订单明细
 * Created by bingshanguxue on 15/9/22.
 */
public class GoodsOrderItem extends MfhEntity<Long> implements Item {
    private Long orderId; //订单ID
//    private Long productId; //产品编号
//    private Long skuId; //商品sku编号
    private Double price; //单价

    //订单数量，总价
    private Double bcount; //个数
    private Double amount; //总价

    /**
     * 订单实际发生数量，总价
     * 对应配送单的组货
     * */
    private Double commitCount;
    private Double commitAmount;

    private String unitName;//单位名称
    private String barcode;//订单编号
    private String productName;
    private String imgUrl;//图片


//    private String remark; //备注

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
        if (bcount == null){
            return 0D;
        }
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

    public Double getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(Double commitCount) {
        this.commitCount = commitCount;
    }

    public Double getCommitAmount() {
        return commitAmount;
    }

    public void setCommitAmount(Double commitAmount) {
        this.commitAmount = commitAmount;
    }
}
