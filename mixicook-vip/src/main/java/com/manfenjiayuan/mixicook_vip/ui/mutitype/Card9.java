package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import com.mfh.framework.api.anon.sc.storeRack.CardProduct;

import java.io.Serializable;
import java.util.List;

import me.drakeet.multitype.Item;

/**
 * 类目商品卡片 3*2
 * Created by bingshanguxue on 10/10/2016.
 */

public class Card9 implements Serializable, Item {
    private Integer type;//卡片类型不同样式布局
    private Long frontCategoryId;//lnktype为1时前台类目编号
    private String categoryName;//类目名称
    private List<CardProduct> products;

    private Long shopId;//店铺编号

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getFrontCategoryId() {
        return frontCategoryId;
    }

    public void setFrontCategoryId(Long frontCategoryId) {
        this.frontCategoryId = frontCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<CardProduct> getProducts() {
        return products;
    }

    public void setProducts(List<CardProduct> products) {
        this.products = products;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }
}
