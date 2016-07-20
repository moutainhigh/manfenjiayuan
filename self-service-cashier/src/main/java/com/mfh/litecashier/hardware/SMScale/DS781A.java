package com.mfh.litecashier.hardware.SMScale;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 5/25/16.
 */
public class DS781A implements Serializable{
    private Double netWeight = 0D;
    private Double tareWeight = 0D;
    private Double unitPrice = 0D;
    private Double totalPrice = 0D;


    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public Double getTareWeight() {
        return tareWeight;
    }

    public void setTareWeight(Double tareWeight) {
        this.tareWeight = tareWeight;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
