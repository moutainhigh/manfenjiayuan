package com.mfh.litecashier.event;

/**
 * 待送订单
 * Created by kun on 15/9/23.
 */
public class OrderStockEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据

    private int affairId;

    public OrderStockEvent(int affairId) {
        this.affairId = affairId;
    }

    public int getAffairId() {
        return affairId;
    }
}
