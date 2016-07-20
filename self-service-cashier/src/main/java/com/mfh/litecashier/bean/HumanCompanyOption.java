package com.mfh.litecashier.bean;

/**
 * 用户信息
 * Created by bingshanguxue on 2015/5/14.
 *
 */
public class HumanCompanyOption implements java.io.Serializable{
    private Long code;
    private String value;

    public HumanCompanyOption(){
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
