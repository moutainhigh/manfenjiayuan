package com.manfenjiayuan.mixicook_vip.wxapi;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 31/10/2016.
 */

public class PayResultWrapper implements Serializable{
    public int errCode;
    public String errStr;
    public String transaction;
    public String openId;

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrStr() {
        return errStr;
    }

    public void setErrStr(String errStr) {
        this.errStr = errStr;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
