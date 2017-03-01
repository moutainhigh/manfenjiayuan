package com.mfh.framework.api.pmcstock;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * Created by bingshanguxue on 25/02/2017.
 */

public class GoodsItem extends MfhEntity<Long> {
    private String barcode;
    private String productName;
    private Double bcount;
    private Double amount;
    private Double factAmount;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public Double getFactAmount() {
        return factAmount;
    }

    public void setFactAmount(Double factAmount) {
        this.factAmount = factAmount;
    }
}
