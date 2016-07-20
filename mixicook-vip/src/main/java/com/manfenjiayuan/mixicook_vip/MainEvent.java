package com.manfenjiayuan.mixicook_vip;

import android.os.Bundle;

/**
 * 事务
 * Created by bingshanguxue on 15/9/23.
 */
public class MainEvent {

    public static final int EID_SHOPCART_DATASET_CHANGED = 0X01;//购物车数量变化

    private int eventId;
    private Bundle args;//参数

    public MainEvent(int eventId) {
        this.eventId = eventId;
    }

    public MainEvent(int eventId, Bundle args) {
        this.eventId = eventId;
        this.args = args;
    }

    public int getEventId() {
        return eventId;
    }

    public Bundle getArgs() {
        return args;
    }
}
