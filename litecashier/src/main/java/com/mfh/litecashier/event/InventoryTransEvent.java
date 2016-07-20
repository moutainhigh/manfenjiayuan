package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * 库存调拨
 * Created by kun on 15/9/23.
 */
public class InventoryTransEvent {
    public static final int EVENT_ID_INIT_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_RELOAD_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_RELAOD_ITEM_DATA = 0X03;//刷新数据

    private int affairId;
    private Bundle args;

    public InventoryTransEvent(int affairId) {
        this.affairId = affairId;
    }


    public InventoryTransEvent(int affairId, Bundle args) {
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
