package com.bingshanguxue.cashier.hardware.scale;

import com.mfh.framework.prefs.SharedPrefesManagerFactory;

/**
 * 电子计价秤
 * <ol>
 *     <li>默认支持爱华电子计价秤(ACS-P215)和寺冈电子秤（DIGI-DS781）两款点子秤</li>
 *     <li>默认使用串口2（/dev/ttymxc1）波特率相同都是9600</li>
 * </ol>
 * Created by bingshanguxue on 27/11/2016.
 */

public class ScaleProvider {
    private static final boolean ENABLED_DEF = true;

    public static final int SCALE_TYPE_ACS_P215 = 0;
    public static final int SCALE_TYPE_DS_781A = 1;
    private static final int SCALE_TYPE_DEF = SCALE_TYPE_ACS_P215;//0,爱华；1,寺冈

    private static final String PORT_DEF = "/dev/ttymxc1";
    private static final String BAUDRATE_DEF = "9600";

    public static final String PREF_NAME = "pref_scale";
    private static final String PK_ENABLED = "PK_ENABLED";
    private static final String PK_SCALE_TYPE = "PK_SCALE_TYPE";

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

    public static int getScaleType(){
        return SharedPrefesManagerFactory.getInt(PREF_NAME, PK_SCALE_TYPE, SCALE_TYPE_DEF);
    }

    public static void setScaleType(int type){
        SharedPrefesManagerFactory.set(PREF_NAME, PK_SCALE_TYPE, type);
    }

    public static String getScaleName(){
        int scaleType = getScaleType();
        if (scaleType == SCALE_TYPE_ACS_P215){
            return "爱华电子计价秤(ACS-P215)";
        }
        else if (scaleType == SCALE_TYPE_DS_781A){
            return "寺冈电子秤（DIGI-DS781）";
        }
        else{
            return null;
        }
    }


}
