package com.bingshanguxue.cashier.model.wrapper;

import java.io.Serializable;

/**
 * 本地资源菜单
 * Created by bingshanguxue on 8/29/16.
 */
public class ResMenu implements Serializable{
    //管理者控制台
    public static final Long ADMIN_MENU_PURCHASE_MANUAL = 54L;//订货
    public static final Long ADMIN_MENU_INVENTORY= 56L;//库存
    public static final Long ADMIN_MENU_ORDERFLOW= 57L;//流水
    public static final Long ADMIN_MENU_RECEIPT= 58L;//单据
    public static final Long ADMIN_MENU_ANALYSIS= 62L;//统计
    public static final Long ADMIN_MENU_DAILYSETTLE= 63L;//日结
    public static final Long ADMIN_MENU_CASHQUOTA= 66L;//现金授权
    public static final Long ADMIN_MENU_SETTINGS= 65L;//设置


    //金丝雀
    public static final Long CANARY_MENU_GOODS    = 71L;//商品
    public static final Long CANARY_MENU_ORDERFLOW= 72L;//流水
    public static final Long CANARY_MENU_CANARY= 74L;//金丝雀
    public static final Long CANARY_MENU_MESSAGE_MGR= 75L;//消息管理器

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

    public ResMenu(Long id, String nameCn, int resId) {
        this.id = id;
        this.nameCn = nameCn;
        this.resId = resId;
    }
}
