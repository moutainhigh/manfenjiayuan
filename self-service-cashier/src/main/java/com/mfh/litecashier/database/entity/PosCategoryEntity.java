package com.mfh.litecashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * POS--商品-- 常卖
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
@Table(name="tb_pos_category")
public class PosCategoryEntity extends MfhEntity<Long> implements ILongId{

    private Long parentId;
    private Long categoryId;//编号
    private String nameCn;//名称
    private String imageUrl;//图片URL

    private int isCloud;//默认为0表示云端前台类目，1--本地前台类目
    private int resId;//本地图片资源


    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getIsCloud() {
        return isCloud;
    }

    public void setIsCloud(int isCloud) {
        this.isCloud = isCloud;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

}
