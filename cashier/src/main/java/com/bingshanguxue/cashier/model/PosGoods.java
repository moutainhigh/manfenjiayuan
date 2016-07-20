package com.bingshanguxue.cashier.model;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.CateApi;
import com.mfh.framework.api.constant.PriceType;

import java.io.Serializable;
import java.util.Date;

/**
 * 存储在pos端的商品信息
 * 适用场景：门店收银机同步云端商品库
 *
 * @author bingshanguxue created on 2015-9-6
 */
public class PosGoods implements ILongId, Serializable {
    private Long id;        //本店最小商品库存单元编号
    private Long proSkuId;  //产品sku编号
    private String barcode; //最小商品库存单元的条形码
    private Long productId; //所属产品spu编号
    private String name;    // 商品名称
    private String unit;    // 单位，如箱、瓶
    private Double costPrice;   // 商品价格
    private Double quantity = 0D; // 商品数量(库存)
    private Date createdDate;
    private Date updatedDate;
    private Long tenantId;      // 租户信息，即微超公司id
    private Long providerId;    //商品供应商编号
    private Integer status;     //1-有效，默认，0-无效
    private Integer priceType = PriceType.PIECE;//价格类型0-计件 1-计重
    private Double packageNum = 0D;//箱规
    private Long procateId;     //商品类目
    private Integer cateType = CateApi.BACKEND_CATE_BTYPE_NORMAL;   //商品类目的类型


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getStatus() {
        if (status == null) {
            return 1;
        }
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPriceType() {
        if (priceType == null) {
            return PriceType.PIECE;
        }
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public Double getPackageNum() {
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    public Long getProcateId() {
        return procateId;
    }

    public void setProcateId(Long procateId) {
        this.procateId = procateId;
    }

    public Integer getCateType() {
        return cateType;
    }

    public void setCateType(Integer cateType) {
        this.cateType = cateType;
    }
}
