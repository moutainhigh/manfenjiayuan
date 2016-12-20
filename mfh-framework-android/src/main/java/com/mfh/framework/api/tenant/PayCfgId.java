package com.mfh.framework.api.tenant;

import java.io.Serializable;

/**
 * 支付配置参数
 * Created by bingshanguxue on 16/12/2016.
 */

public class PayCfgId implements Serializable {
    private int payType;
    private String chId;

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public String getChId() {
        return chId;
    }

    public void setChId(String chId) {
        this.chId = chId;
    }
}
