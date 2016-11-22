package com.manfenjiayuan.pda_supermarket.ui.pay;

import android.os.Bundle;

/**
 * 支付事件
 */
public class PayEvent {
    public static final int EVENT_ID_SCAN_PAYCODE         = 0X01;//扫描支付条码（会员，支付宝，微信）

    private int action;

    public static final String KEY_PAYCODE   = "paycode"; //支付码
    private Bundle args;

    public PayEvent(int action, Bundle args) {
        this.args = args;
        this.action = action;
    }

    public PayEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Bundle getArgs() {
        return args;
    }
}