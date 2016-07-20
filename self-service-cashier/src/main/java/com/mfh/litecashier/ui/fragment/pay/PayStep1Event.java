package com.mfh.litecashier.ui.fragment.pay;

import android.os.Bundle;

/**
 * 支付事件
 */
public class PayStep1Event {
    public static final int PAY_ACTION_WAYTYPE_UPDATED      = 0X02;//支付方式更新，广播应收金额。
    public static final int PAY_ACTION_PAYSTEP_PROCESS          = 0X03;//支付处理中
    public static final int PAY_ACTION_PAYSTEP_FINISHED    = 0X04;//支付金额发生变化（支付成功）
    public static final int PAY_ACTION_PAYSTEP_FAILED    = 0X05;//支付金额发生变化（失败/异常)

    private int action;

    public static final String KEY_CASHIERORDERINFO   = "CashierOrderInfo"; //订单信息
    public static final String KEY_PAYMENT_INFO   = "paymentInfo";  //支付信息
    public static final String KEY_ERROR_MESSAGE    = "errorMessage";    //错误信息
    public static final String KEY_DISCOUNT_INFO  = "discountInfo"; //支付信息
    public static final String KEY_OUTTRADENO   = "outTradeNo"; //商户订单号
    public static final String KEY_PAY_TYPE     = "payType";    //支付方式
    public static final String KEY_PAY_SUBTYPE     = "paySubType";    //支付方式
    public static final String KEY_MEMBERINFO   = "memberInfo"; //会员信息
    public static final String KEY_CARD_ID     = "cardId";    //卡芯片号
    public static final String KEY_AMOUNT       = "amount";     //支付金额
    public static final String KEY_PAY_STATUS   = "payStatus";  //支付状态
    private Bundle args;

    public PayStep1Event(int action, Bundle args) {
        this.args = args;
        this.action = action;
    }

    public PayStep1Event(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Bundle getArgs() {
        return args;
    }
}