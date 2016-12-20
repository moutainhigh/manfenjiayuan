package com.mfh.litecashier.ui.fragment.order;

import android.os.Bundle;

/**
 * 订单
 * Created by bingshanguxue on 15/9/23.
 */
public class OrderEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_LOAD_POSORDER_ITEMS = 0X02;//加载订单明细
    public static final int EVENT_ID_LOAD_SENDORDER_ITEMS = 0X03;//加载订单明细



    private int eventId;
    private Bundle args;

    public OrderEvent(int eventId) {
        this.eventId = eventId;
    }

    public OrderEvent(int eventId, Bundle args) {
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
