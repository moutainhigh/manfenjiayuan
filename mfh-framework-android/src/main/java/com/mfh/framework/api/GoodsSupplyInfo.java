package com.mfh.framework.api;

import java.io.Serializable;

/**
 * 商品相关的批发商信息
 * Created by bingshanguxue on 16/3/18.
 */
public class GoodsSupplyInfo implements Serializable{
    private Long supplyId;
    private String supplyName = "";
    private Double buyPrice = 0D;//配销价
    private Double startNum = 0D;//起配量
    private Double packageNum = 0D;//箱规
    private Long otherTenantSkuId;

    public Long getSupplyId() {
        return supplyId;
    }

    public void setSupplyId(Long supplyId) {
        this.supplyId = supplyId;
    }

    public String getSupplyName() {
        return supplyName;
    }

    public void setSupplyName(String supplyName) {
        this.supplyName = supplyName;
    }

    public Double getBuyPrice() {
        if (buyPrice == null){
            return 0D;
        }
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getStartNum() {
        if (startNum == null){
            return 0D;
        }
        return startNum;
    }

    public void setStartNum(Double startNum) {
        this.startNum = startNum;
    }

    public Double getPackageNum() {
        if (packageNum == null){
            return 0D;
        }
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    public Long getOtherTenantSkuId() {
        return otherTenantSkuId;
    }

    public void setOtherTenantSkuId(Long otherTenantSkuId) {
        this.otherTenantSkuId = otherTenantSkuId;
    }
}
