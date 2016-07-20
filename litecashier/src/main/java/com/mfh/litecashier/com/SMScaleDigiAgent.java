package com.mfh.litecashier.com;

import com.mfh.framework.helper.SharedPreferencesManager;

/**
 * Created by bingshanguxue on 5/18/16.
 */
public class SMScaleDigiAgent {
    public static String PORT_SCALE_DS781 = "/dev/ttymxc1";
    public static String BAUDRATE_SCALE_DS781 = "9600";

    private static final String PREF_NAME = "pref_scale_ds781";
    private static final String PK_SCALE_PORT = "pref_scale_port";
    private static final String PK_SCALE_BAUDRATE = "pref_scale_baudrate";


    public static final String HEX_TERMINATION_CR = "0D";//CR The end of data 0x0d
    public static final String HEX_TERMINATION_LF = "0A";//LF The end of Text 0x0a
    public static final String HEX_HEADER_0 = "30";//‘0’ Net Price 0x30
    public static final String HEX_HEADER_4 = "34";//‘4’ Tare Price 0x34
    public static final String HEX_HEADER_U = "55";//‘U’ Unit Price 0x55
    public static final String HEX_HEADER_T = "54";//‘T’ Total Price 0x54

    static {
        initialize();
    }

    public static void initialize(){
        PORT_SCALE_DS781 = SharedPreferencesManager.getText(
                PREF_NAME, PK_SCALE_PORT, "");
        BAUDRATE_SCALE_DS781 = SharedPreferencesManager.getText(
                PREF_NAME, PK_SCALE_BAUDRATE, "9600");
    }

    public static void setPort(String port){
        SharedPreferencesManager.set(PREF_NAME, PK_SCALE_PORT, port);
    }

    public static void setBaudrate(String baudrate){
        SharedPreferencesManager.set(
                PREF_NAME, PK_SCALE_BAUDRATE, baudrate);
    }
}
