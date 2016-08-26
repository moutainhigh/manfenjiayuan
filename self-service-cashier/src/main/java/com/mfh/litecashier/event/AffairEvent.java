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

    public static final int EVENT_ID_LOCK_POS_CLIENT = 0X05;//锁定POS机器
    public static final int EVENT_ID_PRE_LOCK_POS_CLIENT = 0X06;//锁定POS机器提示
    public static final int EVENT_ID_UNLOCK_POS_CLIENT = 0X07;//锁定POS机器
    public static final int EVENT_ID_RESET_CASHIER = 0X08;//初始化收银
    public static final int EVENT_ID_CASHIER_FRONTCATA_GOODS = 0X09;//前台类目收银

    //同步数据
    public static final int EVENT_ID_FACTORY_DATA_RESET = 0X20;//恢复出厂设置
    public static final int EVENT_ID_SYNC_DATA_START = 0X21;//同步数据开始
    public static final int EVENT_ID_REDIRECT_TO_LOGIN = 0X11;//跳转至登录页面

    //常用商品
    public static final int EVENT_ID_SHOW_EXPRESS = 0X11;//寄快递

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
