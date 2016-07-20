package com.mfh.owner.bean;

/**
 * Created by Administrator on 2015/6/9.
 */
public class AdvertisePic  implements java.io.Serializable {
    private String imageUrl;
    private String redirectUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
