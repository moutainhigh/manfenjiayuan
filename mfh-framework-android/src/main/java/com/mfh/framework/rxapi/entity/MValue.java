package com.mfh.framework.rxapi.entity;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 06/01/2017.
 */

public class MValue<T> implements Serializable {
    private T val;

    public T getVal() {
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }
}
