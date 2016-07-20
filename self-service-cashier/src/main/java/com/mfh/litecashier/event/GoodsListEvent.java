package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 线上销售
 * Created by bingshanguxue on 15/9/23.
 */
public class GoodsListEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_REMOVE_ITEM = 0X03;// 删除数据

    private int eventId;
    private Bundle args;

    public GoodsListEvent(int eventId) {
        this.eventId = eventId;
    }

    public GoodsListEvent(int eventId, Bundle args) {
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
