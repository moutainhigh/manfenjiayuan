package com.mfh.litecashier.database.entity;

import com.mfh.framework.core.MfhEntity;
import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;

/**
 * POS--商品-- 常卖
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="tb_pos_procuct_commonly_2")
public class CommonlyGoodsEntity extends MfhEntity<Long> implements ILongId{
//    private Long id;//最小商品库存单元编号

    private Long goodsId;//商品主键
    private Long proSkuId;//产品sku编号
    private String barcode; //最小商品库存单元的条形码
    private Long productId;//所属产品spu编号
    private String name; // 商品名称
    private String unit; // 单位，如箱、瓶
    private Double costPrice; // 商品价格
    private Double quantity = 0.0; // 商品数量(库存)
    private Long tenantId;// 租户信息，即微超公司id
    private Long providerId;//商品供应商编号
    private String imgUrl;//图片链接
    private Integer priceType;//价格类型0-计件 1-计重

    private Long categoryId;// 用于分类显示
//    当云端下架或删除一个商品时，并未真正删除商品，而是相当于把status修改成0。如果是物理删除目前没有办法增量同步到pos端。pos端下单时需要自行判断注意只有status=1的商品才能购买
    private int status;//1-有效，默认，0-无效

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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
