package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;
import java.util.Date;

/**
 * 一码多品，一品多码
 * 查询箱规条码对应的主条码
 *
 * @author NAT.ZZN(bingshanguxue) created on 2015-9-6
 */
public class ProductSkuBarcode implements ILongId, Serializable {
    private Long id;
    private String mainBarcode;
    private String otherBarcode;
    private Integer packFlag;
    private Long tenantId;
    private String createdBy;
    private Date createdDate;
    private String updatedBy;
    private Date updatedDate;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMainBarcode() {
        return mainBarcode;
    }

    public void setMainBarcode(String mainBarcode) {
        this.mainBarcode = mainBarcode;
    }

    public String getOtherBarcode() {
        return otherBarcode;
    }

    public void setOtherBarcode(String otherBarcode) {
        this.otherBarcode = otherBarcode;
    }

    public Integer getPackFlag() {
        return packFlag;
    }

    public void setPackFlag(Integer packFlag) {
        this.packFlag = packFlag;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
