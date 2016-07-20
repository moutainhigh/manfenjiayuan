package com.mfh.petitestock.bean;

import java.io.Serializable;

/**
 * 库存商品<br>
 *     shi
 * Created by bingshanguxue on 4/28/16.
 */
public class InvSkuGoods implements Serializable{
    private Long id;//本店最小商品库存单元编号
    private Long rackNo;//货架编号
    private Double quantity;//库存
    private Double lowerLimit;//安全库存
    private Double upperLimit;//排面库存（门店货架上能摆放的最大商品数量）
    private Double costPrice;//零售价
    private String unit;//单位
    private String name;//名称

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRackNo() {
        return rackNo;
    }

    public void setRackNo(Long rackNo) {
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
}
