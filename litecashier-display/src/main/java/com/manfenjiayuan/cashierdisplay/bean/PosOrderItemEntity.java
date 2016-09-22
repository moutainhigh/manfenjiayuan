package com.manfenjiayuan.cashierdisplay.bean;

import java.io.Serializable;

/**
 * POS--销售订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderItemEntity implements Serializable {
    private String orderBarCode; //订单条形码,关联PosOrderEntity
    private String barcode; //商品条形码,最小商品库存单元的条形码
    private Long goodsId;//商品主键
    private Long productId; //产品编号
    private Long proSkuId; //商品sku编号
    private Double bcount = 0D; //商品数量,quantity
    private Double costPrice = 0D; //商品零售价格

    private String name; // 商品名称

    private String unit; // 单位，如箱、瓶
    private Long providerId;//商品供应商编号
    private Double finalPrice = 0D;//成交价
    private int priceType;//价格类型0-计件 1-计重
    private Double amount = 0D; //总价.按零售价计算得出
    private Double finalAmount = 0D; //总价.按成交价计算得出

    public String getOrderBarCode() {
        return orderBarCode;
    }

    public void setOrderBarCode(String orderBarCode) {
        this.orderBarCode = orderBarCode;
    }

    public String getBarcode() {
        if (barcode == null) {
            return "";
        }
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Double getBcount() {
        if (bcount == null) {
            return 0D;
        }
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getCostPrice() {
        if (costPrice == null) {
            return 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }


    public String getName() {
        if (name == null) {
            return "";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public int getPriceType() {
        return priceType;
    }

    public void setPriceType(int priceType) {
        this.priceType = priceType;
    }
}
