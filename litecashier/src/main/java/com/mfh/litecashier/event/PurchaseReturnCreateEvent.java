package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 新建退货单
 * Created by kun on 15/9/23.
 */
public class PurchaseReturnCreateEvent {
    public static final int EVENT_ID_RELOAD_INV_RECVORDER = 0X01;//刷新收货订单

    private int affairId;
    private Bundle args;

    public PurchaseReturnCreateEvent(int affairId) {
        this.affairId = affairId;
    }


    public PurchaseReturnCreateEvent(int affairId, Bundle args) {
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
