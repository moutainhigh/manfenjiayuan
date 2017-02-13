package com.manfenjiayuan.pda_supermarket.bean;

import com.mfh.comn.bean.ILongId;

/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class StockGoods implements ILongId, java.io.Serializable{
    private Long id;
    private Long proSkuId;//产品sku编号
    private Long productId;//所属产品spu编号
    private String barcode;//条码
    private String name;//商品名称
    private Double buyPrice;//配销价
    private Double costPrice;//售价
    private String sellNumber;//销量
    private String imgUrl;//图片链接
//    private Integer vipStatus;//vip等级
//    private Integer mfhSupply;//是否支持满分配送：1-支持；0-不支持
    private Double quantity;//当前库存/数量
    private Double packageNum;//箱包数，规格
    private Double lowerLimit;//最低库存
    private String specNames;//规格名称
    private Long providerId;//供应商ID


    @Override
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Double getBuyPrice() {
        if (buyPrice == null){
            buyPrice = 0D;
        }
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getCostPrice() {
        if (costPrice == null){
            costPrice = 0D;
        }
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public String getSellNumber() {
        return sellNumber;
    }

    public void setSellNumber(String sellNumber) {
        this.sellNumber = sellNumber;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

//    public Integer getVipStatus() {
//        return vipStatus;
//    }
//
//    public void setVipStatus(Integer vipStatus) {
//        this.vipStatus = vipStatus;
//    }

    public Double getQuantity() {
        if (quantity == null){
            quantity = 0D;
        }
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getLowerLimit() {
        if (lowerLimit == null){
            lowerLimit = 0D;
        }
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }
//
//    public Integer getMfhSupply() {
//        return mfhSupply;
//    }
//
//    public void setMfhSupply(Integer mfhSupply) {
//        this.mfhSupply = mfhSupply;
//    }

    public String getSpecNames() {
        return specNames;
    }

    public void setSpecNames(String specNames) {
        this.specNames = specNames;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
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
}
