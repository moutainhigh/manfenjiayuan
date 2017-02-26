package com.mfh.framework.api.productConvert;

import com.mfh.framework.api.abs.MfhEntity;

import java.util.List;

/**
 * 商品转换规则目标商品
 *
 * Created by bingshanguxue on 16/02/2017.
 */
public class ProductConvertWrapper extends MfhEntity<Long> {
    private String convertName;
    private String originProSkuName; //商品名称
    private Long originProSku;//商品编号
    private Double retentionRate;//步留率，损失率
    private List<ProductConvertItem> details;

    private Long saasId;


    public String getConvertName() {
        return convertName;
    }

    public void setConvertName(String convertName) {
        this.convertName = convertName;
    }

    public String getOriginProSkuName() {
        return originProSkuName;
    }

    public void setOriginProSkuName(String originProSkuName) {
        this.originProSkuName = originProSkuName;
    }

    public Long getOriginProSku() {
        return originProSku;
    }

    public void setOriginProSku(Long originProSku) {
        this.originProSku = originProSku;
    }

    public Double getRetentionRate() {
        return retentionRate;
    }

    public void setRetentionRate(Double retentionRate) {
        this.retentionRate = retentionRate;
    }

    public List<ProductConvertItem> getDetails() {
        return details;
    }

    public void setDetails(List<ProductConvertItem> details) {
        this.details = details;
    }

    public Long getSaasId() {
        return saasId;
    }

    public void setSaasId(Long saasId) {
        this.saasId = saasId;
    }
}
