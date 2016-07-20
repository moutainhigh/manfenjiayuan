package com.mfh.litecashier.bean;

import java.util.List;

/**
 * 网点周边的小区
 * Created by Administrator on 2015/5/14.
 *
 */
public class SubdisCode implements java.io.Serializable{
    private List<SubdisCodeOption> options;

    public List<SubdisCodeOption> getOptions() {
        return options;
    }

    public void setOptions(List<SubdisCodeOption> options) {
        this.options = options;
    }
}
