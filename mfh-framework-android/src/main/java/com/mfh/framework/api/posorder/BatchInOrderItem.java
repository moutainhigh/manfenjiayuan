package com.mfh.framework.api.posorder;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 24/04/2017.
 */

public class BatchInOrderItem implements Serializable {
    private Long goodsId;
    private Long productId;
    private Long skuId;
    private String barcode;
    private Double bcount;
    private Double price;
    private Double customerPrice;
    private Double amount;
    private Double factAmount;
    private Double saleAmount;
    private Object ruleAmountMap;
    private Integer prodLineId;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getCustomerPrice() {
        return customerPrice;
    }

    public void setCustomerPrice(Double customerPrice) {
        this.customerPrice = customerPrice;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getFactAmount() {
        return factAmount;
    }

    public void setFactAmount(Double factAmount) {
        this.factAmount = factAmount;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public Object getRuleAmountMap() {
        return ruleAmountMap;
    }

    public void setRuleAmountMap(Object ruleAmountMap) {
        this.ruleAmountMap = ruleAmountMap;
    }

    public Integer getProdLineId() {
        return prodLineId;
    }

    public void setProdLineId(Integer prodLineId) {
        this.prodLineId = prodLineId;
    }
}
