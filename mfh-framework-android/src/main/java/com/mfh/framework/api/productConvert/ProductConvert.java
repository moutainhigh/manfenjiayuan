package com.mfh.framework.api.productConvert;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 商品转换规则
 *
 * Created by bingshanguxue on 16/02/2017.
 */
public class ProductConvert extends MfhEntity<Long> {
    private String convertName; //转换规则名称
    private Long originProSku;//原始商品编号
    private Double retentionRate;//步留率，损失率
    private String saasId; //

    private String captionOriginProSku;//原始商品名称

    public String getConvertName() {
        return convertName;
    }

    public void setConvertName(String convertName) {
        this.convertName = convertName;
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

    public String getSaasId() {
        return saasId;
    }

    public void setSaasId(String saasId) {
        this.saasId = saasId;
    }

    public String getCaptionOriginProSku() {
        return captionOriginProSku;
    }

    public void setCaptionOriginProSku(String captionOriginProSku) {
        this.captionOriginProSku = captionOriginProSku;
    }
}
