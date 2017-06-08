package com.mfh.litecashier.ui.fragment.topup;

import android.os.Bundle;

/**
 * 充值
 */
public class TopupActionEvent {
    public static final int TOPUP_AMOUNT_CHANGED         = 0X01;//充值金额变化
    public static final int TOPUP_PROCESS         = 0X02;//支付宝转账中
    public static final int TOPUP_SUCCEED        = 0X03;//支付宝转账成功
    public static final int TOPUP_ERROR        = 0X04;//支付宝转账失败
    public static final int TOPUP_CUSTOMER         = 0X05;//会员转账

    private int action;

    public static final String KEY_TOPUP_TYPE   = "topupType"; //充值类型
    public static final String KEY_PAY_HUMANID  = "payHumanId";  //充值转账用户
    private Bundle args;

    public TopupActionEvent(int action, Bundle args) {
        this.args = args;
        this.action = action;
    }

    public TopupActionEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public Bundle getArgs() {
        return args;
    }
}