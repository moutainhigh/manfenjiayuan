package com.mfh.framework.uikit.adv;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 5/31/16.
 */
public class AdvLocalPic implements Serializable{
    private int resId;

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public static AdvLocalPic newInstance(int resId){
        AdvLocalPic entity = new AdvLocalPic();
        entity.setResId(resId);
        return entity;
    }

}
