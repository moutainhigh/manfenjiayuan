package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.api.GoodsSupplyInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * SKU商品
 * <p></p>
 * {@link CashierApi#URL_SCGOODSKU_FIND_STOREWITHCHAINSKU 查询批发商采购商品 /scGoodsSku/findStoreWithChainSku}<br>
 * {@link CashierApi#URL_SCGOODSSKU_GETBY_BARCODE 查询发布商品 /scGoodsSku/getByBarcode}<br>
 * {@link CashierApi#URL_SCGOODSSKU_FINDGOODSLIST 查询POS前台类目商品 /scGoodsSku/findGoodsList}<br>
 * {@link CashierApi#URL_SCGOODSSKU_LIST 查询库存商品 /scGoodsSku/list}<br>
 * {@link CashierApi#URL_SCGOODSSKU_GETLOCAL_BYBARCODE 查询库存商品 /scGoodsSku/getLocalByBarcode}<br>
 *
 * @author NAT.ZZN(bingshanguxue) created on 2015-9-6
 */
public class ScGoodsSku implements ILongId, Serializable {
    private Long id;//本店最小商品库存单元编号

    //产品本身信息
    private Long productId;//所属产品spu编号
    private String imgUrl;//图片链接
    private String skuName; // 产品名称
    private String shortName;//产品主规格
    private String unit;            //销售单位，单位，如箱、瓶
    private Integer priceType;      //销售计价类型0-计件 1-计重
    private String buyUnit;         //采购单位
    private Integer buyPriceType;   //采购计价类型

    //产品sku信息
    private Long proSkuId;//产品sku编号
    private String barcode; //产品条形码
    private Integer skuMask;//
    private Double packageNum;//箱规
    private String prodArea; //产地
    private String prodLevel; //等级

    //租户商品sku信息
    private Long tenantId;// 租户信息，即微超公司id
    private Long tenantSkuId;//租户商品SKU编号
    private Integer storeType;//仓储类型
    private Double quantity;     // 商品数量(库存)
    //    private Double sellNumber;//销量
    private Double sellMonthNum;//月销量
    //    private Double sellDayNum;//日销量
    private Double buyPrice;    //采购价,配销价，平均采购价，也就是预计采购价
    private Double costPrice; // 商品售价
    private Double costScore;//商品积分
    private Double startNum;//起配量

    private Double upperLimit;//排面库存
    private Double lowerLimit;//安全库存
    private String specNames;//


    private Date createdDate;
    private Date updatedDate;

//    private Integer status;//1-有效，默认，0-无效
    //有以下属性，其他暂时没有


    //批发商信息
    //chainSkuId: 采购相关功能使用otherTenantSkuId字段值，门店库存相关功能使用tenantSkuId字段值。
//    private Long otherTenantSkuId;//批发商商品SKU编号,chainSkuId
//    private String supplyName;//批发商名称
    private Long providerId;//供应商编号
    private List<GoodsSupplyInfo> supplyItems;//批发商信息


    public Integer getStoreType() {
        return storeType;
    }

    public void setStoreType(Integer storeType) {
        this.storeType = storeType;
    }

    public Double getCostScore() {
        return costScore;
    }

    public void setCostScore(Double costScore) {
        this.costScore = costScore;
    }

    public String getProdArea() {
        return prodArea;
    }

    public void setProdArea(String prodArea) {
        this.prodArea = prodArea;
    }

    public String getProdLevel() {
        return prodLevel;
    }

    public void setProdLevel(String prodLevel) {
        this.prodLevel = prodLevel;
    }

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

//    public Double getSellDayNum() {
//        if (sellDayNum == null){
//            return 0D;
//        }
//        return sellDayNum;
//    }
//
//    public void setSellDayNum(Double sellDayNum) {
//        this.sellDayNum = sellDayNum;
//    }

    public Double getCostPrice() {
        if (costPrice == null) {
            return 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }


    public Double getBuyPrice() {
        if (buyPrice == null) {
            return 0D;
        }
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
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

    public Double getStartNum() {
        if (startNum == null) {
            return 0D;
        }
        return startNum;
    }

    public void setStartNum(Double startNum) {
        this.startNum = startNum;
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
}
