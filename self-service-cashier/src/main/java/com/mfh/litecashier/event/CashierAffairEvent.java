package com.mfh.litecashier.event;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class CashierAffairEvent {
    public static final int EVENT_ID_RESET_CASHIER = 0X02;//初始化收银

    private int affairId;

    public CashierAffairEvent(int affairId) {
        this.affairId = affairId;
    }

    public int getAffairId() {
        return affairId;
    }
}
