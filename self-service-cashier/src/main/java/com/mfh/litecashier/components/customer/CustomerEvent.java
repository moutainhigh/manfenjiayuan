package com.mfh.litecashier.components.customer;

import android.os.Bundle;

/**
 * 会员
 * Created by bingshanguxue on 15/9/23.
 */
public class CustomerEvent {

    public static final int EVENT_ID_CUSTOMER_GOODS_ORDERS_RELOAD = 0X01;//加载订单明细
    public static final int EVENT_ID_CUSTOMER_GOODS_ORDERS_PRINT = 0X02;//加载订单明细
    public static final int EVENT_ID_CUSTOMER_FLOW_RELOAD = 0X03;//初始化数据
    public static final int EVENT_ID_CUSTOMER_FLOW_PRINT = 0X04;//初始化数据

    private int eventId;
    private Bundle args;

    public CustomerEvent(int eventId) {
        this.eventId = eventId;
    }

    public CustomerEvent(int eventId, Bundle args) {
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
