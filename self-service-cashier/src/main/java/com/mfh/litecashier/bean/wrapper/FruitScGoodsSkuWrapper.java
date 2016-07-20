package com.mfh.litecashier.bean.wrapper;

import com.manfenjiayuan.business.bean.GoodsSupplyInfo;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.litecashier.bean.FruitScGoodsSku;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生鲜商品
 * {@link ScGoodsSku 的扩展，自动转取第一个批发商信息}
 * Created by bingshanguxue on 6/6/16.
 */
public class FruitScGoodsSkuWrapper implements Serializable {
    private Long id;//本店最小商品库存单元编号

    //产品本身信息
    private Long productId;//所属产品spu编号
    private String imgUrl;//图片链接
    private String skuName; // 商品名称
    private String shortName;//规格
    private String unit;            //销售单位，单位，如箱、瓶
    private Integer priceType;      //销售计价类型0-计件 1-计重
    private String buyUnit;         //采购单位
    private Integer buyPriceType;   //采购计价类型

    //产品sku信息
    private Long proSkuId;//产品sku编号
    private String barcode; //最小商品库存单元的条形码
    private Integer skuMask;//
    private Double packageNum;//箱规

    //租户商品sku信息
    private Long tenantSkuId;//租户商品SKU编号
    private Double quantity;     // 商品数量(库存)
    private Double sellMonthNum;//月销量
    private Long tenantId;// 租户信息，即微超公司id

    private Double upperLimit;//排面库存
    private Double lowerLimit;//安全库存
    private String specNames;//


    private Date createdDate;
    private Date updatedDate;

//    private Integer status;//1-有效，默认，0-无效
    //有以下属性，其他暂时没有


    //chainSkuId: 采购相关功能使用otherTenantSkuId字段值，门店库存相关功能使用HtenantSkuId字段值。
//    private Long otherTenantSkuId;//批发商商品SKU编号,chainSkuId
//    private String supplyName;//批发商名称
    private Long providerId;//供应商编号
    private List<GoodsSupplyInfo> supplyItems;//批发商信息


    //商品所属批发商信息
    private GoodsSupplyInfo mGoodsSupplyInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantSkuId() {
        return tenantSkuId;
    }

    public void setTenantSkuId(Long tenantSkuId) {
        this.tenantSkuId = tenantSkuId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Double getQuantity() {
        if (quantity == null) {
            return 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

//    public Double getSellNumber() {
//        if (sellNumber == null){
//            return 0D;
//        }
//        return sellNumber;
//    }
//
//    public void setSellNumber(Double sellNumber) {
//        this.sellNumber = sellNumber;
//    }

    public Double getSellMonthNum() {
        if (sellMonthNum == null) {
            return 0D;
        }
        return sellMonthNum;
    }

    public void setSellMonthNum(Double sellMonthNum) {
        this.sellMonthNum = sellMonthNum;
    }


    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getSpecNames() {
        return specNames;
    }

    public void setSpecNames(String specNames) {
        this.specNames = specNames;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getProSkuId() {
        return proSkuId;
    }

    public void setProSkuId(Long proSkuId) {
        this.proSkuId = proSkuId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getSkuMask() {
        return skuMask;
    }

    public void setSkuMask(Integer skuMask) {
        this.skuMask = skuMask;
    }

    public Double getPackageNum() {
        if (packageNum == null) {
            return 0D;
        }
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public List<GoodsSupplyInfo> getSupplyItems() {
        return supplyItems;
    }

    public void setSupplyItems(List<GoodsSupplyInfo> supplyItems) {
        this.supplyItems = supplyItems;

        if (supplyItems != null && supplyItems.size() > 0){
            // TODO: 6/14/16 取第一个批发商信息也是有问题
            setGoodsSupplyInfo(supplyItems.get(0));
        }
        else{
            setGoodsSupplyInfo(null);
        }
    }

    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }

    public Integer getBuyPriceType() {
        return buyPriceType;
    }

    public void setBuyPriceType(Integer buyPriceType) {
        this.buyPriceType = buyPriceType;
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public GoodsSupplyInfo getGoodsSupplyInfo() {
        return mGoodsSupplyInfo;
    }

    public void setGoodsSupplyInfo(GoodsSupplyInfo goodsSupplyInfo) {
        mGoodsSupplyInfo = goodsSupplyInfo;
    }

    public static FruitScGoodsSkuWrapper build(FruitScGoodsSku goodsSku){
        if (goodsSku == null){
            return null;
        }

        FruitScGoodsSkuWrapper wrapper = new FruitScGoodsSkuWrapper();
        //设置批发商信息，预计采购价
        wrapper.setSupplyItems(goodsSku.getSupplyItems());
        wrapper.setImgUrl(goodsSku.getImgUrl());
        wrapper.setSkuName(goodsSku.getSkuName());
        wrapper.setBarcode(goodsSku.getBarcode());
        wrapper.setBuyUnit(goodsSku.getBuyUnit());
        wrapper.setQuantity(goodsSku.getQuantity());
        wrapper.setProSkuId(goodsSku.getProSkuId());
        return wrapper;
    }

    public static List<FruitScGoodsSkuWrapper> buildList(List<FruitScGoodsSku> goodsSkuList){
        if (goodsSkuList == null || goodsSkuList.size() < 1){
            return null;
        }

        List<FruitScGoodsSkuWrapper> wrapperList = new ArrayList<>();
        for (FruitScGoodsSku good : goodsSkuList){
            FruitScGoodsSkuWrapper wrapper = build(good);
            if (good != null){
                wrapperList.add(wrapper);
            }
        }
        return wrapperList;
    }


}
