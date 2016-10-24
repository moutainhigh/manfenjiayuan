package com.mfh.litecashier.ui.prepare;

import android.os.Bundle;

/**
 * 拣货单组货
 */
public class PrepareEvent {
    public static final int ACTION_PREPARE         = 0X05;//组货

    private int action;

    public static final String KEY_SCORDER   = "scOrder"; //订单信息
    private Bundle args;

    public PrepareEvent(int action, Bundle args) {
        this.args = args;
        this.action = action;
    }

    public PrepareEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Bundle getArgs() {
        return args;
    }
}