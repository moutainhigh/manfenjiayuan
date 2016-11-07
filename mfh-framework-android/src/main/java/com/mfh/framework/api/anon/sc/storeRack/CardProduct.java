package com.mfh.framework.api.anon.sc.storeRack;

import com.mfh.comn.bean.PageInfo;

import net.tsz.afinal.http.AjaxCallBack;

import java.io.Serializable;

/**
 * 商品卡片
 * Created by bingshanguxue on 09/10/2016.
 */

public class CardProduct implements Serializable{
    private Integer lnktype;//-1自定义链接0-无效1-前台类目2-商品详情
    private Long goodsId;//lnktype为2时商品编号
    private Long skuId;
    private Long spuId;
    private String barcode;
    private Long categoryId;
    private String name;//商品名称
    private String imageUrl;//图片地址

    /**
     * 以下信息通过调用
     * {@link com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl#findOnlineGoodsList(Long, String, PageInfo, AjaxCallBack)}
     * 接口查询}
     * */
    private String buyUnit;//购买单位
    private Long productId;
    private Double costPrice;//售价

    public Integer getLnktype() {
        return lnktype;
    }

    public void setLnktype(Integer lnktype) {
        this.lnktype = lnktype;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }


    public String getBuyUnit() {
        return buyUnit;
    }

    public void setBuyUnit(String buyUnit) {
        this.buyUnit = buyUnit;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }
}
