package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 库存商品<br>
 * 适用场景：批发商盘点/报损
 * Created by bingshanguxue on 4/28/16.
 */
public class InvSkuGoods extends MfhEntity<Long> {
//    private Long id;//本店最小商品库存单元编号
    private Long proSkuId;
    private String barcode;//商品条码
    private String rackNo;//货架编号
    private Double quantity;//库存
    private Double lowerLimit;//安全库存
    private Double upperLimit;//排面库存（门店货架上能摆放的最大商品数量）
    private Double costPrice;//零售价
    private String unit;//单位
    private String name;//名称

    private Double quantityCheck;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }
}
