package com.mfh.litecashier.event;

/**
 * 库存商品
 * Created by kun on 15/9/23.
 */
public class CommodityStockEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据

    private int affairId;

    public CommodityStockEvent(int affairId) {
        this.affairId = affairId;
    }

    public int getAffairId() {
        return affairId;
    }
}
