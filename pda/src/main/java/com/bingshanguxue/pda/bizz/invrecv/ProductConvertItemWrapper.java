package com.bingshanguxue.pda.bizz.invrecv;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * Created by bingshanguxue on 21/02/2017.
 */

public class ProductConvertItemWrapper  extends MfhEntity<Long> {
    private Long convertId;
    private String proSkuName; //商品名称
    private Long proSkuId;//商品编号
    private Double partRate;//
    private String saasId; //

    private Double price;
    private Double quantityCheck;
    private Double amount;

    public Long getConvertId() {
        return convertId;
    }

    public void setConvertId(Long convertId) {
        this.convertId = convertId;
    }

    public String getProSkuName() {
        return proSkuName;
    }

    public void setProSkuName(String proSkuName) {
        this.proSkuName = proSkuName;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Double getPartRate() {
        return partRate;
    }

    public void setPartRate(Double partRate) {
        this.partRate = partRate;
    }

    public String getSaasId() {
        return saasId;
    }

    public void setSaasId(String saasId) {
        this.saasId = saasId;
    }

    public Double getQuantityCheck() {
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
