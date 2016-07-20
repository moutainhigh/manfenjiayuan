package com.mfh.buyers.bean;

/**
 * Life -- GridView -- Item
 * Created by ZZN  on 2015/4/17.
 */
public class FunctionGridCell {
    private String description;//

    private int picResId;
    private String picUrl;
    private String redirectUrl;//点击跳转网页URL
    private boolean hasDiscount;//是否有折扣


    public FunctionGridCell() {
    }

    public FunctionGridCell(String description, String picUrl, String redirectUrl, boolean hasDiscount){
        this.description = description;
        this.picUrl = picUrl;
        this.redirectUrl = redirectUrl;
        this.hasDiscount = hasDiscount;
    }

    public FunctionGridCell(String description, int picResId, String redirectUrl, boolean hasDiscount){
        this.description = description;
        this.picResId = picResId;
        this.redirectUrl = redirectUrl;
        this.hasDiscount = hasDiscount;
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

    public boolean isHasDiscount() {
        return hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
    }
}
