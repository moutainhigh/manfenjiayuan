package com.bingshanguxue.cashier.model.wrapper;

import java.io.Serializable;

/**
 * 本地资源菜单
 * Created by bingshanguxue on 8/29/16.
 */
public class ResMenu implements Serializable{

    //收银
    public static final Long CASHIER_MENU_ONLINE_ORDER     = 10L;//订单列表
    public static final Long CASHIER_MENU_REGISTER_VIP     = 12L;//注册
    public static final Long CASHIER_MENU_MEMBER_CARD      = 13L;//会员卡（开卡）
    public static final Long CASHIER_MENU_HANGUP_ORDER  = 14L;//挂单
    public static final Long CASHIER_MENU_RETURN_GOODS  = 15L;//退货
    public static final Long CASHIER_MENU_MONEYBOX  = 17L;//钱箱
    public static final Long CASHIER_MENU_BALANCE_QUERY    = 18L;//余额查询
    public static final Long CASHIER_MENU_SETTINGS         = 19L;//设置
    public static final Long CASHIER_MENU_PACKAGE = 20L;//包裹(取快递)
    public static final Long CASHIER_MENU_EXPRESS = 21L;//寄快递
    public static final Long CASHIER_MENU_PRINT_ORDER = 22L;//打印订单
    public static final Long CASHIER_MENU_DISCOUNT = 23L;//订单打折
    public static final Long CASHIER_MENU_SCORE = 24L;//积分兑换

    //管理者控制台
    public static final Long ADMIN_MENU_PURCHASE_MANUAL = 51L;//订货
    public static final Long ADMIN_MENU_INVENTORY= 52L;//库存
    public static final Long ADMIN_MENU_RECEIPT= 54L;//单据
    public static final Long ADMIN_MENU_ANALYSIS= 55L;//统计
    public static final Long ADMIN_MENU_DAILYSETTLE= 56L;//日结
    public static final Long ADMIN_MENU_CASHQUOTA= 57L;//现金授权
    public static final Long ADMIN_MENU_SETTINGS= 58L;//设置
    public static final Long ADMIN_MENU_FACTORYDATA_RESET= 59L;//恢复出厂设置
    public static final Long ADMIN_MENU_SYSTEM_UPGRADE=60L;//系统升级


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
