package com.mfh.framework.helper;

import android.content.SharedPreferences;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.SharedPreferencesUtil;


/**
 * Created by bingshanguxue on 2015/6/17.
 */
public class SharedPreferencesManager {
    //APP
    public static final String PREF_NAME_APP = "PREF_NAME_COMN_APP_v1";
    public static final String PREF_KEY_APP_FIRST_START= "PREF_KEY_APP_FIRST_START";//是否首次启动
    public static final String PREF_KEY_APP_STARTUP_DATETIME= "PREF_KEY_APP_STARTUP_DATETIME";//启动时间
    public static final String PREF_KEY_APP_DAY_FIRST_STARTUP_DATETIME= "PREF_KEY_APP_DAY_FIRST_STARTUP_DATETIME";//当天首次启动时间
    public static final String PREF_KEY_APP_UNIQUE_ID = "PREF_KEY_COMN_APP_UNIQUEID";//App唯一标识
    public static final String PREF_KEY_APP_USERAGENT = "PREF_KEY_COMN_APP_USERAGENT";//UserAgent
    public static final String PREF_KEY_APP_TERMINAL_ID = "PREF_KEY_COMN_APP_TERMINAL_ID";//App唯一终端编号
    public static final String PK_B_SUPER_PERMISSION_GRANTED = "pk_b_SUPER_PERMISSION_GRANTED";//获取超级权限


    //出厂配置
    public static final String PREF_NAME_APP_BORN = "PREF_NAME_APP_BORN_v1";// 应用出厂配置
    public static final String PREF_KEY_APP_BORN_RELEASE_VERSION= "PREF_KEY_APP_BORN_RELEASE_VERSION";//是否是正式发布版本,默认正式发布

    //应用配置
    public static final String PREF_NAME_CONFIG = "PREF_NAME_CONFIG_v1";
    public static final String PREF_KEY_CONFIG_NOTIFICATION_ACCEPT  = "PREF_KEY_CONFIG_NOTIFICATION_ACCEPT";//开启通知
    public static final String PREF_KEY_CONFIG_LOCATION_ACCEPT      = "PREF_KEY_CONFIG_LOCATION_ACCEPT";//开启位置服务
    public static final String PK_B_SOFTKEYBOARD_ENABLED    = "pk_softkeyboard_enabled";//软键盘
    public static final String PK_B_TTS_ENABLED    = "pk_tts_enabled";//语音播报


    //
    public static SharedPreferences getPreferences(String prefName) {
        return SharedPreferencesUtil.getSharedPreferences(MfhApplication.getAppContext(),
                prefName);
    }

    /**
     * 是否正式版本，默认是true
     * */
    public static boolean isReleaseVersion(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_APP_BORN,
                PREF_KEY_APP_BORN_RELEASE_VERSION, true);
    }

    public static void setReleaseVersion(boolean enabled){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_APP_BORN,
                PREF_KEY_APP_BORN_RELEASE_VERSION, enabled);
    }

    public static void setAppFirstStart(boolean enabled){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_FIRST_START, enabled);
    }

    /**
     * 是否首次启动
     * */
    public static boolean isAppFirstStart(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_FIRST_START, true);
    }

    /**
     * 启动时间
     * */
    public static String getAppStartupDateTime(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_STARTUP_DATETIME, "");
    }
    public static void setAppStartupDateTime(String dateTime){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_STARTUP_DATETIME, dateTime);
    }
    /**
     * 当天首次启动时间
     * */
    public static String getAppDayFirstStartupDateTime(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_DAY_FIRST_STARTUP_DATETIME, "");
    }
    public static void setAppDayFirstStartupDateTime(String dateTime){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_APP,
                PREF_KEY_APP_DAY_FIRST_STARTUP_DATETIME, dateTime);
    }

    /**
     * 是否开启位置服务：默认true
     * */
    public static boolean getLocationAcceptEnabled(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PREF_KEY_CONFIG_LOCATION_ACCEPT, true);
    }


    public static void setLocationAcceptEnabled(boolean enabled){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PREF_KEY_CONFIG_LOCATION_ACCEPT, enabled);
    }
    public static void setNotificationAcceptEnabled(boolean enabled){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PREF_KEY_CONFIG_NOTIFICATION_ACCEPT, enabled);
    }

    /**
     * 是否接收通知：默认true
     * */
    public static boolean getNotificationAcceptEnabled(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PREF_KEY_CONFIG_NOTIFICATION_ACCEPT, true);
    }

    public static void setSoftKeyboardEnabled(boolean enabled){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PK_B_SOFTKEYBOARD_ENABLED, enabled);
    }

    /**
     * 是否使用键盘
     * */
    public static boolean isSoftKeyboardEnabled(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(), PREF_NAME_CONFIG,
                PK_B_SOFTKEYBOARD_ENABLED, false);
    }

    /**
     * 获取设备终端编号
     * */
    public static String getTerminalId(){
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                PREF_NAME_APP, PREF_KEY_APP_TERMINAL_ID, "");
    }

    public static void setTerminalId(String id){
        SharedPreferencesUtil.set(MfhApplication.getAppContext(),
                PREF_NAME_APP, PREF_KEY_APP_TERMINAL_ID, id);
    }

    public static String getText(String prefName, String key) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                prefName, key, "");
    }

    public static String getText(String prefName, String key, String defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                prefName, key, defVal);
    }


    public static int getInt(String prefName, String key, int defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.getInt(MfhApplication.getAppContext(),
                prefName, key, defVal);
    }

    public static Long getLong(String prefName, String key, Long defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.getLong(MfhApplication.getAppContext(),
                prefName, key, defVal);
    }
    public static boolean getBoolean(String prefName, String key, boolean defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                prefName, key, defVal);
    }

    public static void set(String prefName, String key, String value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), prefName, key, value);
    }
    public static void set(String prefName, String key, int value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), prefName, key, value);
    }
    public static void set(String prefName, String key, Long value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), prefName, key, value);
    }
    public static void set(String prefName, String key, boolean value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), prefName, key, value);
    }

    public static void clear(String prefName){
        SharedPreferencesUtil.clear(MfhApplication.getAppContext(), prefName);
    }

}
