package com.mfh.enjoycity.events;

/**
 * 微信支付
 * Created by Nat.ZZN(bingshanguxue) on 15/8/18.
 */
public class CreateOrderEvent {

    private int errCode;
    private String errStr;

    public CreateOrderEvent(int errCode, String errStr) {
        this.errCode = errCode;
        this.errStr = errStr;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrStr() {
        return errStr;
    }
}
