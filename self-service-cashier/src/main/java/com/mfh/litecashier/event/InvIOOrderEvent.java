package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 库存批次
 * Created by kun on 15/9/23.
 */
public class InvIOOrderEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_REMOVE_ITEM = 0X03;// 删除数据

    private int eventId;
    private Bundle args;

    public InvIOOrderEvent(int eventId) {
        this.eventId = eventId;
    }

    public InvIOOrderEvent(int eventId, Bundle args) {
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
