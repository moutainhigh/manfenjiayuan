package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class AffairEvent {
    public static final int EVENT_ID_RESET_UNREAD_ORDER = 0X01;//清空未读订单数
    public static final int EVENT_ID_APPEND_UNREAD_ORDER = 0X02;//增加未读订单数
    public static final int EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER = 0X03;//预定订单更新
    public static final int EVENT_ID_APPEND_UNREAD_SKU = 0X04;//预定订单更新

    //同步数据
    public static final int EVENT_ID_SYNC_DATA_INITIALIZE = 0X20;//恢复出厂设置
    public static final int EVENT_ID_SYNC_DATA_START = 0X21;//同步数据开始
    public static final int EVENT_ID_REDIRECT_TO_LOGIN = 0X11;//跳转至登录页面

    //常用商品
    public static final int EVENT_ID_SHOW_COMMONLY = 0X08;//常用商品
    public static final int EVENT_ID_POPBACKSTACK = 0X10;//
    public static final int EVENT_ID_SHOW_EXPRESS = 0X11;//寄快递
    public static final int EVENT_ID_SHOW_LAUNDRY = 0X12;//洗衣服务
    public static final int EVENT_ID_SHOW_FRONT_CATEGORY = 0X14;//显示前台类目，带数据
    public static final int EVENT_ID_HIDE_RIGHTSLIDE = 0X15;//

    private int affairId;
    private Bundle args;//参数

    public AffairEvent(int affairId) {
        this.affairId = affairId;
    }

    public AffairEvent(int affairId, Bundle args) {
        this.affairId = affairId;
        this.args = args;
    }

    public int getAffairId() {
        return affairId;
    }

    public Bundle getArgs() {
        return args;
    }
}
