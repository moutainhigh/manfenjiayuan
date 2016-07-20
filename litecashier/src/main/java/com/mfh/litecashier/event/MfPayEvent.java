package com.mfh.litecashier.event;

import android.os.Bundle;

/**
 * Created by kun on 15/9/7.
 */
public class MfPayEvent {

    public static final int EVENT_ID_QEQUEST_HANDLE_AMOUNT = 0;//请求应付金额
    public static final int EVENT_ID_READ_MFACCOUNT = 1;//输入满分用户信息
    public static final int EVENT_ID_PAY_PROCESSING = 30;//正在进行支付
    public static final int EVENT_ID_PAY_FAILED     = 31;//支付失败(包括异常)
    public static final int EVENT_ID_PAY_SUCCEE     = 32;//支付成功
    /**
     * 0:请求应付价格
     * 10:现金收取金额
     * 20:支付中
     * */
    private int eventId;

    public static final String KEY_OUTTRADENO   = "outTradeNo";//商户订单号
    public static final String KEY_PAY_TYPE     = "payType";//支付方式
    public static final String KEY_AMOUNT       = "amount";//支付金额
    public static final String KEY_PAY_STATUS   = "payStatus";//支付状态
    private Bundle args;

    public MfPayEvent(int eventId, Bundle args) {
        this.args = args;
        this.eventId = eventId;
    }

    public MfPayEvent(int eventId) {
        this.eventId = eventId;
        this.args = null;
    }

    public int getEventId() {
        return eventId;
    }

    public Bundle getArgs() {
        return args;
    }
}
