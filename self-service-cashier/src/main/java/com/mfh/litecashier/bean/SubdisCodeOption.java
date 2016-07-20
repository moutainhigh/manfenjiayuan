package com.mfh.litecashier.bean;

/**
 * 网点周边的小区
 * Created by bingshanguxue on 2015/5/14.
 * {
 *  "access": "0",
 *  "code": 241,
 *  "hasChild": "false",
 *  "levelName": "",
 *  "value": "荣域"
 *  }
 */
public class SubdisCodeOption implements java.io.Serializable{
    private Long code;
    private String value;

    public SubdisCodeOption(){
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
