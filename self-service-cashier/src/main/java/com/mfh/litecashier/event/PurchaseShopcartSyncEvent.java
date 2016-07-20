package com.mfh.litecashier.event;

/**
 * 采购商品购物车
 * Created by bingshanguxue on 15/9/23.
 */
public class PurchaseShopcartSyncEvent {
    public static final int EVENT_ID_DATASET_CHANGED    = 0X01;//数据发生改变
    public static final int EVENT_ID_ORDER_SUCCESS      = 0X02;//下单成功

    private int eventId;

    public PurchaseShopcartSyncEvent(int eventId) {
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return String.format("[PurchaseShopcartSyncEvent: eventId=%d]", eventId);
    }
}
