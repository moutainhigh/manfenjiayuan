package com.bingshanguxue.cashier.model.wrapper;

import java.io.Serializable;

/**
 * 本地资源菜单
 * Created by bingshanguxue on 8/29/16.
 */
public class ResMenu implements Serializable{


    //管理者控制台
    public static final Long ADMIN_MENU_PURCHASE_MANUAL = 51L;//订货
    public static final Long ADMIN_MENU_INVENTORY= 52L;//库存
    public static final Long ADMIN_MENU_RECEIPT= 54L;//单据
    public static final Long ADMIN_MENU_ANALYSIS= 55L;//统计
    public static final Long ADMIN_MENU_RECONCILE=56L;//系统升级
    public static final Long ADMIN_MENU_GOODSFLOW= 57L;//商品流水
    public static final Long ADMIN_MENU_CASHQUOTA= 58L;//现金授权
    public static final Long ADMIN_MENU_SETTINGS= 59L;//设置
    public static final Long ADMIN_MENU_FACTORYDATA_RESET= 60L;//恢复出厂设置
    public static final Long ADMIN_MENU_SYSTEM_UPGRADE=61L;//系统升级


    //金丝雀
    public static final Long CANARY_MENU_GOODS    = 71L;//商品档案
    public static final Long CANARY_MENU_ORDERFLOW= 72L;//流水
    public static final Long CANARY_MENU_MESSAGE_MGR= 74L;//消息管理器

    private Long id;//编号
    private String nameCn;//名称
    private int resId;//本地图片资源
    private int badgeNumber = 0;

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

    public int getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public ResMenu(Long id, String nameCn, int resId) {
        this.id = id;
        this.nameCn = nameCn;
        this.resId = resId;
    }
}
