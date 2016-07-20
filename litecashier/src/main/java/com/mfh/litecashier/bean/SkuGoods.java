package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.CashierApi;

import java.io.Serializable;
import java.util.Date;

/**
 * SKU商品
 *{@link CashierApi#URL_CHAINGOODSSKU_FIND_PUBLICCHAINGOODSSKU}
 * @author Nat.zzn(bingshanguxue) created on 2015-9-6
 */
public class SkuGoods implements ILongId, Serializable {

//    {
//        "imgId": null,
//            "imgUrl": null,
//            "specNames": null,
//            "attList": null,
//            "companyName": "满分家园",
//            "providerName": "",
//            "quantity": null,
//            "sellNum": null,
//            "hintPrice": null,
//            "sendUnit": 0,
//            "startNum": null,
//            "proSkuId": 120,
//            "barcode": "11223344556677",
//            "skuName": "淮河边上的红酒",
//            "unit": "",
//            "productId": 777,
//            "skuMask": 0,
//            "status": 1,
//            "priceType": 0,
//            "costPrice": 120,
//            "packageNum": null,
//            "providerId": null,
//            "procateId": 375,
//            "buyPrice": 120,
//            "tenantId": 130222,
//            "saasId": 130222,
//            "id": 2005,
//            "createdBy": "",
//            "createdDate": null,
//            "updatedBy": "",
//            "updatedDate": "2016-01-11 22:37:44"
//    }

    private Long id;//本店最小商品库存单元编号,chainSkuId

    private Long productId;//所属产品spu编号
    private String imgUrl;//图片链接
    private String skuName; // 商品名称
    private String unit; // 单位，如箱、瓶
    private Integer priceType;//价格类型0-计件 1-计重

    private Long proSkuId;//产品sku编号
    private String barcode; //最小商品库存单元的条形码
    private Double packageNum;//箱规

    private Double quantity = 0D; // 商品数量(库存)
    private Double sellNum = 0D;//销量
    private Double buyPrice = 0D;//配销价
    private Double costPrice = 0D; // 零售价
//    private Long companyId;//供应商编号（使用tenantId代替，@Nat_20160112）
    private String companyName;//供应商名称
    private Long tenantId;// 租户信息，即微超公司id
    private Date createdDate;
    private Date updatedDate;

    private Double startNum;//起配量
    private Integer status;//1-有效，默认，0-无效
    //有以下属性，其他暂时没有


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

    public Double getSellNum() {
        if (sellNum == null){
            return 0D;
        }
        return sellNum;
    }

    public void setSellNum(Double sellNum) {
        this.sellNum = sellNum;
    }

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

//    public Long getCompanyId() {
//        return companyId;
//    }
//
//    public void setCompanyId(Long companyId) {
//        this.companyId = companyId;
//    }


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
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
}
