package com.mfh.framework.rxapi.entity;

import java.util.List;

/**
 * Created by bingshanguxue on 26/12/2016.
 */

public class MRspQuery <T> {
    private long total = 0;//全部的结果记录数
    private List<T> rows;


    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
