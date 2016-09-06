package com.bingshanguxue.cashier.hardware.scale;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 5/25/16.
 */
public class DS781A implements Serializable{

    private static final int DATAFORMAT_A_TOTAL_LENGTH = 37;
    public static final String HEX_TERMINATION_CR = "0D";//CR The end of data 0x0d
    public static final String HEX_TERMINATION_LF = "0A";//LF The end of Text 0x0a
    public static final String HEX_HEADER_0 = "30";//‘0’ Net Price 0x30
    public static final String HEX_HEADER_4 = "34";//‘4’ Tare Price 0x34
    public static final String HEX_HEADER_U = "55";//‘U’ Unit Price 0x55
    public static final String HEX_HEADER_T = "54";//‘T’ Total Price 0x54

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
