package com.mfh.litecashier.bean.wrapper;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;

/**
 * 寄快递服务
 * Created by Nat.ZZN(bingshanguxue) on 15/9/2.
 */
public class CashierExpressInfo implements ILongId, Serializable {

    private Long id;//编号
    private String nameCn;//名称
    private int resId;//本地图片资源

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public static CashierExpressInfo newInstance(Long id, String nameCn, int resId){
        CashierExpressInfo category = new CashierExpressInfo();
        category.id = id;
        category.resId = resId;
        category.nameCn = nameCn;
        return category;
    }

}
