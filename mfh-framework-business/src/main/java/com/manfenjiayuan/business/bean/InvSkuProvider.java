package com.manfenjiayuan.business.bean;

/**
 * 供应链商品
 * Created by bingshanguxue on 5/29/16.
 */
public class InvSkuProvider {
//    private Long id;
    private String barcode;
    private String skuName;
    private Long productId;
    private Long proSkuId;
    private Double costPrice;
    private String unit;
    private Long providerId;
    private Long tenantSkuId;

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
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

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Long getTenantSkuId() {
        return tenantSkuId;
    }

    public void setTenantSkuId(Long tenantSkuId) {
        this.tenantSkuId = tenantSkuId;
    }
}
