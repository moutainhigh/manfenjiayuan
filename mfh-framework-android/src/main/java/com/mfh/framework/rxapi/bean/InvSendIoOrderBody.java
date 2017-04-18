package com.mfh.framework.rxapi.bean;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 01/04/2017.
 */

public class InvSendIoOrderBody implements Serializable {
    private Long otherOrderId;
    private String checkOk;
    private String jsonStr;

    public Long getOtherOrderId() {
        return otherOrderId;
    }

    public void setOtherOrderId(Long otherOrderId) {
        this.otherOrderId = otherOrderId;
    }

    public String getCheckOk() {
        return checkOk;
    }

    public void setCheckOk(String checkOk) {
        this.checkOk = checkOk;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }
}
