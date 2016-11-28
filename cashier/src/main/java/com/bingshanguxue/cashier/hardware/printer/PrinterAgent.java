package com.bingshanguxue.cashier.hardware.printer;

import com.mfh.framework.prefs.SharedPrefesManagerFactory;


/**
 *
 * Created by bingshanguxue on 27/11/2016.
 */

public class PrinterAgent {
    private static final boolean ENABLED_DEF = false;

    public static final int PRINTER_TYPE_COMMON = 0;
    public static final int PRINTER_TYPE_EMBEDED = 1;
    private static final int PRINTER_TYPE_DEF = PRINTER_TYPE_COMMON;//0,外接；1,嵌入式
    private static final String PORT_DEF = "/dev/ttymxc0";
    private static final String BAUDRATE_DEF = "9600";

    public static final String PREF_NAME = "pref_printer";
    private static final String PK_ENABLED = "PK_ENABLED";
    private static final String PK_PRINTER_TYPE = "PK_PRINTER_TYPE";


    public static String getPort(){
        return PORT_DEF;
    }

    public static String getBaudrate() {
        return BAUDRATE_DEF;
    }

    public static boolean isEnabled(){
        return SharedPrefesManagerFactory.getBoolean(PREF_NAME, PK_ENABLED, ENABLED_DEF);
    }

    public static void setEnabled(boolean enabled){
        SharedPrefesManagerFactory.set(PREF_NAME, PK_ENABLED, enabled);
    }

    public static int getPrinterType(){
        return SharedPrefesManagerFactory.getInt(PREF_NAME, PK_PRINTER_TYPE, PRINTER_TYPE_DEF);
    }

    public static void setPrinterType(int type){
        SharedPrefesManagerFactory.set(PREF_NAME, PK_PRINTER_TYPE, type);
    }

    public static String getPrinterName(){
        int scaleType = getPrinterType();
        if (scaleType == PRINTER_TYPE_COMMON){
            return "分离式";
        }
        else if (scaleType == PRINTER_TYPE_EMBEDED){
            return "嵌入式";
        }
        else{
            return null;
        }
    }

}
