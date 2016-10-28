package com.manfenjiayuan.pda_supermarket.ui.common;

import android.os.Bundle;

public class ScOrderEvent {
    public static final String EXTRA_KEY_SCORDER = "scOrder";
    public static final int EVENT_ID_UPDATE = 0X01;//商品刷新
    public static final int EVENT_ID_DATASETCHANGED = 0X02;//商品刷新

    private int eventId;
    private Bundle args;//参数

    public ScOrderEvent(int eventId) {
        this.eventId = eventId;
    }

    public ScOrderEvent(int eventId, Bundle args) {
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