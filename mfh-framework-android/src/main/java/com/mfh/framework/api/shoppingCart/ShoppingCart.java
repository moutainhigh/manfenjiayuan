package com.mfh.framework.api.shoppingCart;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车
 * Created by bingshanguxue on 9/29/16.
 */

public class ShoppingCart implements Serializable {
    private Long shopId;//店铺编号
    private String shopName;//店铺名
    private List<CartPack> products;//商品明细

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public List<CartPack> getProducts() {
        return products;
    }

    public void setProducts(List<CartPack> products) {
        this.products = products;
    }
}
