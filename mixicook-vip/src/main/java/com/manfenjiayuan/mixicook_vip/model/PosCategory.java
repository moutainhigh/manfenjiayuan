package com.manfenjiayuan.mixicook_vip.model;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;

/**
 * POS前台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/9/2.
 */
public class PosCategory implements ILongId, Serializable {
    private Long parentId;
    private Long id;//编号
    private String nameCn;//名称
    private String imageUrl;//图片URL

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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
}
