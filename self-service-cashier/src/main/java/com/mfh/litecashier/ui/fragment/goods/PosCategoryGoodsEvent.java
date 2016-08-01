package com.mfh.litecashier.ui.fragment.goods;

import android.os.Bundle;

/**
 * 类目商品
 * Created by bingshanguxue on 15/9/23.
 */
public class PosCategoryGoodsEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_SORT_RESET = 0X03;//刷新数据
    public static final int EVENT_ID_SORT_UPDATE = 0X04;//刷新数据

    private int eventId;
    private Bundle args;

    public PosCategoryGoodsEvent(int eventId) {
        this.eventId = eventId;
    }

    public PosCategoryGoodsEvent(int eventId, Bundle args) {
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