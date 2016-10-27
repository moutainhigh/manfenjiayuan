package com.manfenjiayuan.mixicook_vip.ui.home;

import android.os.Bundle;

import com.manfenjiayuan.mixicook_vip.utils.AddCartOptions;

/**
 * Created by bingshanguxue on 27/10/2016.
 */

public class ShopcartEvent {
    public static final int EVENT_ID_ADD2CART             = 0X01;//加入购物车
    public static final int EVENT_ID_DATASETCHANGED        = 0X02;//数据变化

    private int eventId;
    private Bundle args;//参数
    private AddCartOptions mCartOptions;

    public ShopcartEvent(int eventId) {
        this.eventId = eventId;
    }

    public ShopcartEvent(int eventId, Bundle args) {
        this.eventId = eventId;
        this.args = args;
    }

    public ShopcartEvent(int eventId, AddCartOptions cartOptions) {
        this.eventId = eventId;
        this.mCartOptions = cartOptions;
    }

    public int getEventId() {
        return eventId;
    }

    public Bundle getArgs() {
        return args;
    }

    public AddCartOptions getCartOptions() {
        return mCartOptions;
    }
}
