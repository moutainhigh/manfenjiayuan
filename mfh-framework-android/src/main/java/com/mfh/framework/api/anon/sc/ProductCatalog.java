package com.mfh.framework.api.anon.sc;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 前台类目商品关系表
 * Created by bingshanguxue on 8/15/16.
 */
public class ProductCatalog extends MfhEntity<Long> {
    private Long paramValueId;//前台类目编号
    private Long cataItemId;//商品编号(spuId/productId)，也可能是关联的后台类目编号
    private Integer subType;//关联后台类目是为1，商品时为0

    public Long getParamValueId() {
        return paramValueId;
    }

    public void setParamValueId(Long paramValueId) {
        this.paramValueId = paramValueId;
    }

    public Long getCataItemId() {
        return cataItemId;
    }

    public void setCataItemId(Long cataItemId) {
        this.cataItemId = cataItemId;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }
}
