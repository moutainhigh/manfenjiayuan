package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;

/**
 * Created by bingshanguxue on 7/17/16.
 */
public class PurchaseFreshEvent {

    /**
     * 同步商品库档案:
     * 注意此操作只会同步批发商的商品库到门店的商品库，POS机的商品库同步是另外一个逻辑
     * */
    public static final int EVENT_ID_SYNC_START = 0X01;
    private int eventId;
    private Bundle args;

    public PurchaseFreshEvent(int eventId) {
        this.eventId = eventId;
    }

    public PurchaseFreshEvent(int eventId, Bundle args) {
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
