package com.manfenjiayuan.pda_wholesaler.bean.wrapper;

import java.io.Serializable;

/**
 * Created by kun on 15/9/2.
 */
public class HomeMenu implements Serializable{
    public static final Long OPTION_ID_GOODS            = 10L;//商品
    public static final Long OPTION_ID_STOCK_TAKE   = 3L;//盘点（修改当前库存）
    public static final Long OPTION_ID_DISTRIBUTION = 4L;//收货（商品配送）
    public static final Long OPTION_ID_WHOLESALER_GOODS_SHELVES     = 11L;//商品货架绑定
    public static final Long OPTION_ID_WHOLESALER_PICKING_GOODS     = 12L;//拣货
public static final Long OPTION_ID_CREATE_INV_LOSSORDER    = 13L;//报损订单
    public static final Long OPTION_ID_STOCK_IN = 20L;//入库
    public static final Long OPTION_ID_STOCK_OUT = 21L;//出库
    public static final Long OPTION_ID_ALPHA    = 22L;//


    private Long id;
    private String name;
    private int resId;

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
}
