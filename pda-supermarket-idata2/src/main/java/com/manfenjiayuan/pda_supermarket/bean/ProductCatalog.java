package com.manfenjiayuan.pda_supermarket.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class ProductCatalog implements Serializable{
    private Long id;
    private Long paramValueId;//类目编号
    private Long cataItemId;//spuId,productId
    private Date createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
