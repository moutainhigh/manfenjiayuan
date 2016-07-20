package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 *  本地订单流水
 * Created by kun on 15/9/23.
 */
public class OrderflowLocalWarehouseEvent {
    public static final int EVENT_ID_RELOAD_DATA = 0X01;//初始化数据
    public static final int EVENT_ID_REFRESH_DATA = 0X02;//刷新数据
    public static final int EVENT_ID_RELAOD_ITEM_DATA = 0X03;//刷新数据

    private int affairId;
    private Bundle args;

    public OrderflowLocalWarehouseEvent(int affairId) {
        this.affairId = affairId;
    }


    public OrderflowLocalWarehouseEvent(int affairId, Bundle args) {
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
