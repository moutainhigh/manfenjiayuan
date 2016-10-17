package com.manfenjiayuan.mixicook_vip.ui.order;

/**
 * 微信支付
 * Created by bingshanguxue on 15/8/18.
 */
public class WxPayEvent {

    private int errCode;
    private String errStr;

    public WxPayEvent(int errCode, String errStr) {
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
