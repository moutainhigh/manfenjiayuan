package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 商城订单明细
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderItem extends MfhEntity<Long> {
    private String productName;//商品名称
    private String unitName;//单位
    private Double bcount;//数量
    private Double price;//价格

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
}
