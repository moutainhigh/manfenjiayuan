package com.mfh.framework.api.anon.storeRack;

import java.io.Serializable;

import me.drakeet.multitype.Item;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class StoreRackCardItem implements Serializable, Item {
    private Integer lnktype;//-1自定义链接0-无效1-前台类目2-商品详情
    private String link;//自定义链接
    private Long imageId;//图片编号
    private String imageUrl;//图片地址url
    private Long frontCategoryId;//lnktype为1时前台类目编号
    private Long goodsId;//lnktype为2时商品编号
    private String name;//类目或商品名称

    public Integer getLnktype() {
        return lnktype;
    }

    public void setLnktype(Integer lnktype) {
        this.lnktype = lnktype;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getFrontCategoryId() {
        return frontCategoryId;
    }

    public void setFrontCategoryId(Long frontCategoryId) {
        this.frontCategoryId = frontCategoryId;
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
}

