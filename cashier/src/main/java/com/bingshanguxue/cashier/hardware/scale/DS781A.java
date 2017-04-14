package com.bingshanguxue.cashier.hardware.scale;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 5/25/16.
 */
public class DS781A implements Serializable{


    //Control Code and Characters
    /**CR The end of data 0x0d 回车*/
    public static final byte TERMINATION_CR = 0X0D;
    /**LF The end of Text 0x0a 换行*/
    public static final byte TERMINATION_LF = 0X0A;
    public static final byte DATA_MINUS = 0X2D;//‘‐‘ (Minus),Minus sign
    public static final byte DATA_DECIMAL = 0X2E;//‘.’ (Decimal), Decimal
    public static final byte DATA_SPACE = 0X20;//‘ ’(Space), Data error or empty
    public static final byte[] DATA_OF = new byte[]{0x4f, 0x46};//Overflow
    public static final byte[] DATA_UF = new byte[]{0x55, 0x46};//Underflow
    /**‘0’ Net Weight 0x30*/
    public static final byte HEADER_NET_WEIGHT = 0X30;
    public static final byte HEADER_TARE_WEIGHT = 0X34;//‘4’ Tare Weight 0x34
    public static final byte HEADER_UNIT_PRICE = 0X55;//‘U’ Unit Price 0x55
    public static final byte HEADER_TOTAL_PRICE = 0X54;//‘T’ Total Price 0x54
    public static final byte COMMAND_ENQ = 0X05;//ENQ Enquiry 0x05
    public static final byte COMMAND_ACK = 0X06;//ACK Acknowledge 0x06
    public static final byte COMMAND_NAK = 0X15;//ACK Acknowledge 0x06


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
