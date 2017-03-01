package com.mfh.framework.api;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 配方
 * Created by bingshanguxue on 22/02/2017.
 */

public class ProductStructure extends MfhEntity<Long> {
    private Long masterSkuId;//源商品编号
    private Long partSkuId;//配方商品编号
    private String partSkuName;//配方商品名称
    private Double partNum;//配方商品用量
    private String partUnit;//配方商品单位

    public Long getMasterSkuId() {
        return masterSkuId;
    }

    public void setMasterSkuId(Long masterSkuId) {
        this.masterSkuId = masterSkuId;
    }

    public Long getPartSkuId() {
        return partSkuId;
    }

    public void setPartSkuId(Long partSkuId) {
        this.partSkuId = partSkuId;
    }

    public String getPartSkuName() {
        return partSkuName;
    }

    public void setPartSkuName(String partSkuName) {
        this.partSkuName = partSkuName;
    }

    public Double getPartNum() {
        return partNum;
    }

    public void setPartNum(Double partNum) {
        this.partNum = partNum;
    }

    public String getPartUnit() {
        return partUnit;
    }

    public void setPartUnit(String partUnit) {
        this.partUnit = partUnit;
    }
}
