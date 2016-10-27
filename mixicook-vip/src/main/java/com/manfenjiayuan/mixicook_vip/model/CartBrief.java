package com.manfenjiayuan.mixicook_vip.model;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 27/10/2016.
 */

public class CartBrief implements Serializable{
    private Double skuNum;
    private Double bcount;
    private Double amount;

    public Double getSkuNum() {
        return skuNum;
    }

    public void setSkuNum(Double skuNum) {
        this.skuNum = skuNum;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
