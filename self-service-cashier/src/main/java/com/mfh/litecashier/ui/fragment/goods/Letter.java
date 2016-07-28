package com.mfh.litecashier.ui.fragment.goods;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class Letter implements Serializable{
    private String value;
    private String name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
