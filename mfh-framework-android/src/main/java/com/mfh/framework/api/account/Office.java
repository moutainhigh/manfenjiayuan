package com.mfh.framework.api.account;

import java.io.Serializable;

/**
 * 部门
 * Created by bingshanguxue on 2014/9/11.
 */
public class Office implements Serializable{
    private String stockId;
    private Long code;
    private String value;

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
