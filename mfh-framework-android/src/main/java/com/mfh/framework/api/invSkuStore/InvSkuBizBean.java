package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 库存商品<br>
 * 适用场景：批发商盘点/报损
 * Created by bingshanguxue on 4/28/16.
 */
public class InvSkuBizBean extends MfhEntity<Long> {
//    private Long id;//本店最小商品库存单元编号
    private String rackNo;//货架编号
    private Double quantity;//库存
    private Double lowerLimit;//安全库存
    private Double upperLimit;//排面库存（门店货架上能摆放的最大商品数量）
    private Integer priceType;//计价类型
    private Double buyPrice;//库存成本价
    private Double costPrice;//零售价
    private Double customerPrice;//会员价
    private Double costScore;//商品积分

    private String unit;//单位
    private String skuName;//名称
    private String shortName;//规格

    //产品sku信息
    private Long proSkuId;//产品sku编号
    private String barcode; //产品条形码
    private String prodArea; //产地
    private String prodLevel; //等级


    private Double avgSellNum = 0D;//过去三十天总销量

//    private Double quantityCheck;

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRackNo() {
        return rackNo;
    }

    public void setRackNo(String rackNo) {
        this.rackNo = rackNo;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getCustomerPrice() {
        return customerPrice;
    }

    public void setCustomerPrice(Double customerPrice) {
        this.customerPrice = customerPrice;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }


    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getCostScore() {
        return costScore;
    }

    public void setCostScore(Double costScore) {
        this.costScore = costScore;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getProdArea() {
        return prodArea;
    }

    public void setProdArea(String prodArea) {
        this.prodArea = prodArea;
    }

    public String getProdLevel() {
        return prodLevel;
    }

    public void setProdLevel(String prodLevel) {
        this.prodLevel = prodLevel;
    }

    public Double getAvgSellNum() {
        if (avgSellNum == null) {
            return 0D;
        }
        return avgSellNum;
    }

    public void setAvgSellNum(Double avgSellNum) {
        this.avgSellNum = avgSellNum;
    }
}
