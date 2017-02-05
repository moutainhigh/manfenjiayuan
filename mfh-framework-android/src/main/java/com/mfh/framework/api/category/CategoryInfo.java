package com.mfh.framework.api.category;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 类目信息
 * Created by bingshanguxue on 8/15/16.
 */
public class CategoryInfo extends MfhEntity<Long> {
//    private Long id;//编号
    private Long parentId;
    private String imageUrl;//图片URL
    private String nameCn;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }
}
