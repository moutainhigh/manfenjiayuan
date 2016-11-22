package com.mfh.litecashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.invOrder.InvOrderApi;

/**
 * <ol>
 * 采购订单购物车商品明细，对应{@link InvOrderApi#URL_INVSENDORDER_ASK_SENDORDER}
 * <li>生鲜采购</li>
 * </ol>
 * Created by <bingshanguxue> on 16-06-02
 */
@Table(name = "tb_purchase_goods_v0100")
public class PurchaseGoodsEntity extends MfhEntity<Long> implements ILongId {
    /**
     * 采购类型，用来区别该商品属于哪类订单
     */
    private Integer purchaseType;//采购类型

    /**
     * 批发商编号，用来支持按批发商拆分订单
     */
    private Long providerId;//商品供应商编号
    private String providerName;
    private Long chainSkuId;//


    /**
     * {@link com.mfh.framework.api.constant.IsPrivate}
     */
    private Integer isPrivate;

    private Long proSkuId; //产品spu编号,productId
    private String barcode = ""; //最小商品库存单元的条形码
    private String imgUrl;//图片链接
    private String productName = "";    // 商品名称
    private String unit = "";    // 单位，如箱、瓶
    private Double buyPrice = 0D;   //价格
    private Double quantityCheck = 0D;   // 商品数量


    public Integer getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(Integer purchaseType) {
        this.purchaseType = purchaseType;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Long getChainSkuId() {
        return chainSkuId;
    }

    public void setChainSkuId(Long chainSkuId) {
        this.chainSkuId = chainSkuId;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public Double getQuantityCheck() {
        if (quantityCheck == null) {
            return 0D;
        }
        return quantityCheck;
    }

    public void setQuantityCheck(Double quantityCheck) {
        this.quantityCheck = quantityCheck;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

}
