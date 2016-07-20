package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 订货
 * Created by kun on 15/9/23.
 */
public class OrderGoodsEvent {
    public static final int EVENT_ID_SHOW_CATEGORY = 0X01;//显示类目
    public static final int EVENT_ID_HIDE_CATEGORY = 0X02;//隐藏类目
    public static final int EVENT_ID_UPDATE_PLATFORM_PROVIDER = 0X03;//更新平台商


    private int eventId;
    public Bundle args;//参数

    public OrderGoodsEvent(int eventId) {
        this.eventId = eventId;
    }

    public OrderGoodsEvent(int eventId, Bundle args) {
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
