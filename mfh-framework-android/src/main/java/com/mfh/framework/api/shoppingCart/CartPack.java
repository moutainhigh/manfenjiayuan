package com.mfh.framework.api.shoppingCart;

import java.io.Serializable;

/**
 * 购物车商品明细
 * Created by bingshanguxue on 9/29/16.
 */

public class CartPack implements Serializable{
    private Cart cart;//商品信息
    private String productName;//商品名
    private String unitName;//单位
    private String imgUrl;//商品图片
    private Long imgId;//商品图片

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Long getImgId() {
        return imgId;
    }

    public void setImgId(Long imgId) {
        this.imgId = imgId;
    }
}
