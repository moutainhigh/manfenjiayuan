package com.mfh.framework.rxapi.entity;

/**
 * 统一封装返回结果
 * Created by [bingshanguxue@gmail.com] on 4/12/16.
 */
public class MResponse<T>{
    protected String msg        = "";   // 返回码的描述
    protected int code      = 0;   // 返回码。
    protected int version   = 0;
    protected T data            = null; // 数据部分

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
