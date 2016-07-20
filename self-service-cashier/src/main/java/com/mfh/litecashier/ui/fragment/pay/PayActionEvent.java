package com.mfh.litecashier.ui.fragment.pay;

import android.os.Bundle;

/**
 * 支付事件
 */
public class PayActionEvent {
    public static final int PAY_ACTION_VIP_DETECTED         = 0X05;//检测（读取）到会员信息
    public static final int PAY_ACTION_BASIC_PAY            = 0X06;//普通支付

    private int action;

    public static final String KEY_CASHIERORDERINFO   = "CashierOrderInfo"; //订单信息
    public static final String KEY_PAYMENT_INFO   = "paymentInfo";  //支付信息
    public static final String KEY_PAY_TYPE     = "payType";    //支付方式
    public static final String KEY_PAY_SUBTYPE     = "paySubType";    //支付方式
    public static final String KEY_MEMBERINFO   = "memberInfo"; //会员信息
    public static final String KEY_CARD_ID     = "cardId";    //卡芯片号
    public static final String KEY_ERROR_MESSAGE    = "errorMessage";    //错误信息
    private Bundle args;

    public PayActionEvent(int action, Bundle args) {
        this.args = args;
        this.action = action;
    }

    public PayActionEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Bundle getArgs() {
        return args;
    }
}