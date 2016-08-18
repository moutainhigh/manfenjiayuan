package com.mfh.framework.api.invFindOrder;


import com.mfh.framework.api.constant.IsPrivate;

import java.io.Serializable;

/**
 *  拣货单明细
 * Created by bingshanguxue on 15/9/22.
 */
public class InvFindOrderItem implements Serializable {

    private Long id;
    private Long tenantSkuId;//所属租户skuid,对应发货单中的chainSkuId
    private Long proSkuId;//产品sku编号，引用产品sku表
    private String productName;//商品名称
    private Double quantityCheck;//配送数量
    private Double price;//价格
    private Double amount;//总价
    private String unitSpec;//单位
    private String barcode;//条码
    private String imgUrl;//图片
    private Long providerId;//供应商编号
    private Integer isPrivate = IsPrivate.PLATFORM;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getQuantityCheck() {
        if (quantityCheck == null){
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public Double getPrice() {
        if (price == null){
            return 0D;
        }
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAmount() {
        if (amount == null){
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUnitSpec() {
        if (unitSpec == null){
            return "";
        }
        return unitSpec;
    }

    public void setUnitSpec(String unitSpec) {
        this.unitSpec = unitSpec;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getTenantSkuId() {
        return tenantSkuId;
    }

    public void setTenantSkuId(Long tenantSkuId) {
        this.tenantSkuId = tenantSkuId;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }
}
