package com.mfh.litecashier.hardware.AHScale;

import com.mfh.framework.helper.SharedPreferencesManager;

/**
 * 爱华电子计价秤
 * Created by bingshanguxue on 5/27/16.
 */
public class AHScaleAgent {
    //秤(ACS-P215计价秤)
    public static String PORT_ACS_P215 = "/dev/ttyS0";
    public static String BAUDRATE_ACS_P215 = "9600";

    private static final String PREF_NAME = "pref_ah_scale";
    private static final String PK_PORT_ACS_P215 = "pk_port_acs_p215";
    private static final String PK_BAUDRATE_ACS_P215 = "pk_baudrate_acs_p215";

    static {
        initialize();
    }

    public static void initialize() {
        PORT_ACS_P215 = SharedPreferencesManager.getText(
                PREF_NAME, PK_PORT_ACS_P215, "");
        BAUDRATE_ACS_P215 = SharedPreferencesManager.getText(
                PREF_NAME, PK_BAUDRATE_ACS_P215, "9600");
    }

    public static void setAcsP215Port(String port) {
        SharedPreferencesManager.set(PREF_NAME, PK_PORT_ACS_P215, port);
    }

    public static void setAcsP215Baudrate(String baudrate) {
        SharedPreferencesManager.set(
                PREF_NAME, PK_BAUDRATE_ACS_P215, baudrate);
    }

}
