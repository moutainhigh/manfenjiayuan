package com.manfenjiayuan.mixicook_vip.wxapi;

import android.os.Bundle;

/**
 * Created by bingshanguxue on 31/10/2016.
 */

public class PayEvent {
    public static final int EVENT_ID_ONPAYRESP  = 0X01;//支付完成处理结果

    private int eventId;
    private Bundle args;//参数

    private PayResultWrapper mPayResultWrapper;

    public PayEvent(int eventId) {
        this.eventId = eventId;
    }

    public PayEvent(int eventId, Bundle args) {
        this.eventId = eventId;
        this.args = args;
    }

    public PayEvent(int eventId, PayResultWrapper payResultWrapper) {
        this.eventId = eventId;

        mPayResultWrapper = payResultWrapper;
    }

    public int getEventId() {
        return eventId;
    }

    public Bundle getArgs() {
        return args;
    }

    public PayResultWrapper getPayResultWrapper() {
        return mPayResultWrapper;
    }
}
