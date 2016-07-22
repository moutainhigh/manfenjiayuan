package com.manfenjiayuan.business.bean;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApi;

import java.util.Date;

/**
 * 盘点商品/报损商品
 * {@link ScChainGoodsSkuApi.URL_SCGOODSSKU_FINDBY_BARCODE,}
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 */
public class StockTakeGoods implements ILongId, java.io.Serializable {

    private Long id;//chainSkuId,tenantSkuId
    private Long proSkuId;//产品sku编号
    private Long productId;//所属产品spu编号
    private String imgUrl;//图片链接
    private String skuName;//商品名称
    private String shortName;//规格
    private String barcode;//商品条码
    private Double quantity;//当前库存/数量
    private Integer priceType;//价格类型0-计件 1-计重
    private String unit; // 单位，如箱、瓶
    //    private Double sellNumber;//销量
    private Double buyPrice;//配销价
    private Double costPrice;//零售价
    //    private Long providerId;//商品供应商编号
//    private String providerName;//商品供应商名称

    private Integer skuMask;
    private Double packageNum;//箱规

    private Long tenantId;// 租户信息，即微超公司id
    protected Date createdDate;
    private Date updatedDate;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }


    public Double getBuyPrice() {
        if (buyPrice == null) {
            buyPrice = 0D;
        }
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getCostPrice() {
        if (costPrice == null) {
            costPrice = 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

//    public Double getSellNumber() {
//        if (sellNumber == null) {
//            sellNumber = 0D;
//        }
//        return sellNumber;
//    }
//
//    public void setSellNumber(Double sellNumber) {
//        this.sellNumber = sellNumber;
//    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Double getQuantity() {
        if (quantity == null) {
            quantity = 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

//    public Long getProviderId() {
//        return providerId;
//    }
//
//    public void setProviderId(Long providerId) {
//        this.providerId = providerId;
//    }
//
//    public String getProviderName() {
//        return providerName;
//    }
//
//    public void setProviderName(String providerName) {
//        this.providerName = providerName;
//    }

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

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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
}
