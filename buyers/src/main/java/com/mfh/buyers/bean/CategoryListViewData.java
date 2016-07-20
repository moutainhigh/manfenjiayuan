package com.mfh.buyers.bean;

/**
 * Created by Administrator on 2015/4/21.
 */
public class CategoryListViewData {
    private String title;
    private String detailUrl;

    public CategoryListViewData(String title, String detailUrl){
        this.title = title;
        this.detailUrl = detailUrl;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }
}
