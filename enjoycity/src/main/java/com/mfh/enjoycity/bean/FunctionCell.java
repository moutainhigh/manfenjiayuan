package com.mfh.enjoycity.bean;

/**
 * GridView Item
 * Created by ZZN  on 2015/4/17.
 */
public class FunctionCell{
    private String description;//

    private int picResId;
    private String picUrl;
    private String redirectUrl;//点击跳转网页URL


    public FunctionCell() {
    }

    public FunctionCell(String description, String picUrl, String redirectUrl){
        this.description = description;
        this.picUrl = picUrl;
        this.redirectUrl = redirectUrl;
    }

    public FunctionCell(String description, int picResId, String redirectUrl){
        this.description = description;
        this.picResId = picResId;
        this.redirectUrl = redirectUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public int getPicResId() {
        return picResId;
    }

    public void setPicResId(int picResId) {
        this.picResId = picResId;
    }

}
