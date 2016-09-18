package com.mfh.litecashier.bean.wrapper;

import com.mfh.comn.bean.ILongId;
import com.mfh.litecashier.bean.PosCategory;

import java.io.Serializable;

/**
 * 服务台－－功能菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/9/2.
 */
public class CashierFunctional implements ILongId, Serializable {


    private int type = 0;//0:local;1-category
    private Long id;//编号
    private String nameCn;//名称

    private int resId;//本地图片资源
    private String imageUrl;//
    private int badgeNumber = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    //local
    public static CashierFunctional generate(Long id, String nameCn, int resId){
        CashierFunctional entity = new CashierFunctional();
        entity.type = 0;
        entity.id = id;
        entity.resId = resId;
        entity.nameCn = nameCn;
        return entity;
    }

    public static CashierFunctional generate(PosCategory category){
        CashierFunctional entity = new CashierFunctional();
        entity.type = 1;
        entity.id = category.getId();
        entity.nameCn = category.getNameCn();
        entity.imageUrl = category.getImageUrl();
        return entity;
    }


}
