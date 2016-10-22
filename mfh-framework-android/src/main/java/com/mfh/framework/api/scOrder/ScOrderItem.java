package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.constant.PriceType;

/**
 * 商城订单明细
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderItem extends MfhEntity<Long> {
    private Long skuId;
    private String productName;//商品名称
    private String unitName;//单位
    private Double bcount;//数量
    private Double price;//价格
    private Double amount;//金额
    private Integer priceType = PriceType.PIECE;//计价类型

    private Double commitCount;
    private Double commitAmount;

    //以下字段为app新增字段
    private Double quantityCheck;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
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

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}
