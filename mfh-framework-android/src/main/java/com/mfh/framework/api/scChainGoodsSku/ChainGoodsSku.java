package com.mfh.framework.api.scChainGoodsSku;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;

/**
 * 批发商商品
 *
 * @author bingshanguxue created on 2015-9-6
 */
public class ChainGoodsSku implements ILongId, Serializable {
    private Long id;        //本店最小商品库存单元编号
    private Long proSkuId;  //产品sku编号
    private Long productId; //所属产品spu编号
    private String imgUrl;  //图片链接
    private String skuName; // 商品名称
    private String barcode; //最小商品库存单元的条形码
    private Double quantity = 0D; // 商品数量(库存)
    private String unit = "";   // 单位，如箱、瓶
//    private Double sellNum = 0D;//销量
    private String buyUnit;
    private Double buyPrice = 0D;   //门店采购价
    private Double costPrice = 0D;  //批发商的配销价
    private Double singleStartNum;  //给门店的单件起送数量
    private Double singleCostPrice; //给门店的单件批发价
    private Double hintPrice = 0D;//批发商的建议零售价
    private String companyName = "";//批发商名称
    private Long tenantId;      // 租户信息，即微超公司id,供应商编号
//    private Date createdDate;
//    private Date updatedDate;

//    private Double startNum = 0D;//起配量
    private Integer status;     //1-有效，默认，0-无效
    //有以下属性，其他暂时没有
    private Integer priceType;  //价格类型0-计件 1-计重
    private Double packageNum;  //箱规

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getQuantity() {
        if (quantity == null){
            return 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

//    public Double getSellNum() {
//        if (sellNum == null){
//            return 0D;
//        }
//        return sellNum;
//    }
//
//    public void setSellNum(Double sellNum) {
//        this.sellNum = sellNum;
//    }

    public Double getCostPrice() {
        if (costPrice == null){
            return 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
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

    public Double getSingleStartNum() {
        return singleStartNum;
    }

    public void setSingleStartNum(Double singleStartNum) {
        this.singleStartNum = singleStartNum;
    }

    public Double getSingleCostPrice() {
        return singleCostPrice;
    }

    public void setSingleCostPrice(Double singleCostPrice) {
        this.singleCostPrice = singleCostPrice;
    }

    public Double getHintPrice() {
        return hintPrice;
    }

    public void setHintPrice(Double hintPrice) {
        this.hintPrice = hintPrice;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

//    public Double getStartNum() {
//        if (startNum == null){
//            return 0D;
//        }
//        return startNum;
//    }
//
//    public void setStartNum(Double startNum) {
//        this.startNum = startNum;
//    }

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

    public Integer getStatus() {
        if (status == null){
            return 1;
        }
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
//
//    public Date getCreatedDate() {
//        return createdDate;
//    }
//
//    public void setCreatedDate(Date createdDate) {
//        this.createdDate = createdDate;
//    }
//
//    public Date getUpdatedDate() {
//        return updatedDate;
//    }
//
//    public void setUpdatedDate(Date updatedDate) {
//        this.updatedDate = updatedDate;
//    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getPriceType() {
        if (priceType == null){
            return 0;
        }
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

    public Double getPackageNum() {
        if (packageNum == null){
            return 0D;
        }
        return packageNum;
    }

    public void setPackageNum(Double packageNum) {
        this.packageNum = packageNum;
    }

    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }
}
