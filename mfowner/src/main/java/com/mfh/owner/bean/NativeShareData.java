package com.mfh.owner.bean;

/**
 * 分享
 * Created by Administrator on 2015/5/14.
 *
 */
public class NativeShareData implements java.io.Serializable{
    private String currentUrl;//当前页Url，用于校验
    private String shareUrl;//分享页Url
    private String title;//标题
    private String description;//描述
    private String imageUrl;//图片Url

    public NativeShareData(){
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }


    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



}
