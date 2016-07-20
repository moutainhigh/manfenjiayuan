package com.mfh.litecashier.bean.wrapper;

import java.io.Serializable;

/**
 * 广告
 * Created by Nat.ZZN(bingshanguxue) on 15/9/30.
 */
public class AdvertiseBean implements Serializable {
    public static final int ADV_TYPE_SIMPLE = 0;
    public static final int ADV_TYPE_MULTI = 1;

    private String imageUrl;//图片
    private String title;//标题
    private String subTitle;//副标题
    private int advType;//广告类型

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }


    public int getAdvType() {
        return advType;
    }

    public void setAdvType(int advType) {
        this.advType = advType;
    }


    public static AdvertiseBean newInstance(int advType, String imageUrl, String title, String subTitle){
        AdvertiseBean bean = new AdvertiseBean();
        bean.setAdvType(advType);
        bean.setImageUrl(imageUrl);
        bean.setTitle(title);
        bean.setSubTitle(subTitle);
        return bean;
    }

}
