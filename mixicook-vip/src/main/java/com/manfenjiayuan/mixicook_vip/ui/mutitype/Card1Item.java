package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import java.io.Serializable;

import me.drakeet.multitype.Item;

/**
 * Created by bingshanguxue on 10/10/2016.
 */

public class Card1Item implements Serializable, Item {
    private String imageUrl;
    private Integer lnktype;//-1自定义链接0-无效1-前台类目2-商品详情
    private String link;//自定义链接

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

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
}
