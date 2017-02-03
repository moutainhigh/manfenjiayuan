package com.mfh.framework.rxapi.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by bingshanguxue on 26/12/2016.
 */

public class MEntityWrapper<T> implements Serializable{
    private T bean;
    private Map<String, String> caption;

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public Map<String, String> getCaption() {
        return caption;
    }

    public void setCaption(Map<String, String> caption) {
        this.caption = caption;
    }
}
