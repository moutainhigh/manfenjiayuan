package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 新建收货单
 * Created by kun on 15/9/23.
 */
public class PurchaseReceiptCreateEvent {
    public static final int EVENT_ID_RELOAD_INV_SENDORDER = 0X01;//刷新采购订单

    private int affairId;
    private Bundle args;

    public PurchaseReceiptCreateEvent(int affairId) {
        this.affairId = affairId;
    }


    public PurchaseReceiptCreateEvent(int affairId, Bundle args) {
        this.affairId = affairId;
        this.args = args;
    }

    public int getAffairId() {
        return affairId;
    }

    public Bundle getArgs() {
        return args;
    }

    public void setArgs(Bundle args) {
        this.args = args;
    }
}
