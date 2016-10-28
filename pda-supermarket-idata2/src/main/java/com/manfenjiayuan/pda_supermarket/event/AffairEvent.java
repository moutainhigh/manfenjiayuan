package com.manfenjiayuan.pda_supermarket.event;

import android.os.Bundle;

/**
 * 事务
 * Created by kun on 15/9/23.
 */
public class AffairEvent {
    public static final int EVENT_ID_APPEND_UNREAD_SKU = 0X04;//预定订单更新

    public static final int EVENT_ID_BUYER_PREPAREABLE = 0X05;//买手有新订单可抢单


    private int affairId;
    private Bundle args;//参数

    public AffairEvent(int affairId) {
        this.affairId = affairId;
    }

    public AffairEvent(int affairId, Bundle args) {
        this.affairId = affairId;
        this.args = args;
    }

    public int getAffairId() {
        return affairId;
    }

    public Bundle getArgs() {
        return args;
    }
}
