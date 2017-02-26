package com.mfh.framework.api.productConvert;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 商品转换规则目标商品
 *
 * Created by bingshanguxue on 16/02/2017.
 */
public class ProductConvertItem extends MfhEntity<Long> {
    private Long convertId;
    private String proSkuName; //商品名称
    private Long proSkuId;//商品编号
    private Double partRate;//
    private String saasId; //

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

}
