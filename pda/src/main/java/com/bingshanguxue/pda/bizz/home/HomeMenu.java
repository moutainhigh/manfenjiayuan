package com.bingshanguxue.pda.bizz.home;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 15/9/2.
 */
public class HomeMenu implements Serializable {

    public static final Long OPTION_ID_GOODS = 1L;//商品（修改售价和标准库存）
    public static final Long OPTION_ID_PACKAGE = 2L;//包裹(取快递)
    public static final Long OPTION_ID_INVCHECKORDER_STOCKTAKE = 3L;//盘点（修改当前库存）
    public static final Long OPTION_ID_CREATE_INV_RETURNORDER = 5L;//采购退货
    public static final Long OPTION_ID_BIND_GOODS_2_TAGS = 11L;//商品绑定标签
    public static final Long OPTION_ID_WHOLESALER_GOODS_SHELVES     = 12L;//商品货架绑定
    public static final Long OPTION_ID_WHOLESALER_PICKING_GOODS     = 13L;//拣货

    public static final Long OPTION_ID_STOCK_IN = 20L;//入库
    public static final Long OPTION_ID_STOCK_OUT = 21L;//出库
    public static final Long OPTION_ID_CREATE_INV_LOSSORDER = 22L;//报损
    public static final Long OPTION_ID_INVLOSSORDER_STOCKTAKE = 23L;//报损盘点
    public static final Long OPTION_ID_INV_CONVERT = 24L;//库存转换
    public static final Long OPTION_ID_PRINT_TAGS = 25L;//价签打印
    public static final Long OPTION_ID_STORE_IN = 26L;//商品建档
    public static final Long OPTION_ID_CASHIER = 27L;//收银
    public static final Long OPTION_ID_QUERY_BILL = 28L;//单据查询
    public static final Long OPTION_ID_SENDORDER_NEW = 29L;//订货

    public static final Long OPTION_ID_RECVORDER_NEW = 4L;//收货（商品配送）
    public static final Long OPTION_ID_RECVORDER_CONVERT = 31L;//收货转换

    public static final Long OPTION_ID_BUY_SCORDER = 40L;//买手——订单列表
    public static final Long OPTION_ID_BUY_PREPARE = 41L;//买手——组货


    public static final Long OPTION_ID_INSTOCK_SCORDER = 50L;//骑手——订单列表
    public static final Long OPTION_ID_TUOTOU = 51L;//妥投
    public static final Long OPTION_ID_EMBRACE = 52L;//揽件

    public static final Long OPTION_ID_ALPHA = 61L;//


    private Long id;
    private String name;
    private int resId;
    private int badgeNumber = 0;


    public HomeMenu(Long id, String name, int resId) {
        this.id = id;
        this.name = name;
        this.resId = resId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(int badgeNumber) {
        this.badgeNumber = badgeNumber;
    }
}
