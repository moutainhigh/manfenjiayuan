package com.mfh.litecashier.event;

/**
 * 库存报损
 * Created by kun on 15/9/23.
 */
public class StockLossEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据

    private int eventId;

    public StockLossEvent(int eventId) {
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
