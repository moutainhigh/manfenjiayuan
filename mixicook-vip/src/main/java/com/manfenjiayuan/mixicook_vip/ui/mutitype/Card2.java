package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import com.mfh.framework.api.anon.storeRack.CardProduct;
import com.mfh.framework.api.anon.storeRack.StoreRackCardItem;

import java.io.Serializable;
import java.util.List;

import me.drakeet.multitype.Item;

/**
 * Created by bingshanguxue on 10/10/2016.
 */

public class Card2 implements Serializable, Item {
    private Integer type;//卡片类型不同样式布局
    private Long netId;//指定该卡片是在哪个网点购买
    private String netName;//网点名称
    private List<StoreRackCardItem> items;

    //类目card
    private Long frontCategoryId;//lnktype为1时前台类目编号
    private String categoryName;//类目名称
    private List<CardProduct> products;

//    private List<Card2Item> items;
//
//    public List<Card2Item> getItems() {
//        return items;
//    }
//
//    public void setItems(List<Card2Item> items) {
//        this.items = items;
//    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getNetId() {
        return netId;
    }

    public void setNetId(Long netId) {
        this.netId = netId;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
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

    public void setItems(List<StoreRackCardItem> items) {
        this.items = items;
    }

    public List<StoreRackCardItem> getItems() {
        return items;
    }
}
