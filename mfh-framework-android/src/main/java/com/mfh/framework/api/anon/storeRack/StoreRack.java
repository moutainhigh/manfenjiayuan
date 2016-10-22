package com.mfh.framework.api.anon.storeRack;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 货架
 * Created by bingshanguxue on 09/10/2016.
 */

public class StoreRack extends MfhEntity<Long>{
    private Long shopId;//店铺编号
    private String shopName;//店铺名称
    private int rackType;//货架类型
    private String rackName;//货架名称
    private String dataInfo;//
//    private List<StoreRackCard> dataInfo;

    public int getRackType() {
        return rackType;
    }

    public void setRackType(int rackType) {
        this.rackType = rackType;
    }

    public String getRackName() {
        return rackName;
    }

    public void setRackName(String rackName) {
        this.rackName = rackName;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(String dataInfo) {
        this.dataInfo = dataInfo;
    }

//        public List<StoreRackCard> getDataInfo() {
//        return dataInfo;
//    }
//
//    public void setDataInfo(List<StoreRackCard> dataInfo) {
//        this.dataInfo = dataInfo;
//    }
}
