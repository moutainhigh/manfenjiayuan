package com.mfh.litecashier.ui.prepare;

import com.mfh.framework.api.constant.PosType;

import java.io.Serializable;


/**
 * 拣货单订单类型
 * {@link PosType}
 * Created by bingshanguxue on 8/29/16.
 */
public class BizSubTypeWrapper implements Serializable{
    private Integer subType;//编号
    private String name;//名称
    private int resId;//本地图片资源

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public BizSubTypeWrapper(Integer subType, String name, int resId) {
        this.subType = subType;
        this.name = name;
        this.resId = resId;
    }
}
