package com.mfh.framework.api.anon.sc.productPrice;

import com.mfh.framework.api.constant.PriceType;

import java.io.Serializable;

/**
 * 平台商品档案
 * Created by bingshanguxue on 8/30/16.
 */
public class ProductSku implements Serializable {
    private Long id;//proSkuId
    private Long productId;
    private String barcode = "";//商品条码
    private String name = "";//商品名称
    private String unit;//销售单位
    private Integer priceType = PriceType.PIECE;//
    private String buyUnit;//采购单位
    private Integer buyPriceType = PriceType.PIECE;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }

    public Integer getBuyPriceType() {
        return buyPriceType;
    }

    public void setBuyPriceType(Integer buyPriceType) {
        this.buyPriceType = buyPriceType;
    }
}
