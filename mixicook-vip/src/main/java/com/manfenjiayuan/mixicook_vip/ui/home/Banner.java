package com.manfenjiayuan.mixicook_vip.ui.home;

import java.io.Serializable;

/**
 * 广告
 * Created by bingshanguxue on 9/27/16.
 */

public class Banner implements Serializable {
    private String url;//图片地址
    private String link;//跳转链接


    public Banner(String url, String link) {
        this.url = url;
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
