package com.mfh.framework.core.sms;

import android.os.Bundle;

/**
 * 验证码
 * Created by bingshanguxue on 27/10/2016.
 */

public class CaptchasEvent {
    public static final int EVENT_ID_RECEIVE_SMS             = 0X01;//

    private int eventId;
    private Bundle args;//参数

    public CaptchasEvent(int eventId) {
        this.eventId = eventId;
    }

    public CaptchasEvent(int eventId, Bundle args) {
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
