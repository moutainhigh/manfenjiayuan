package com.mfh.framework.prefs;

/**
 * 出厂设置，和登录用户无关
 * Created by bingshanguxue on 2015/6/17.
 */
public class SharedPrefesManagerFactory extends SharedPrefesUltimate {
    //APP出厂设置
    private static final String PREF_NAME_FACTORY = "pref_factory";
    private static final String PK_B_ISRELEASE = "PK_B_ISRELEASE";//是否是正式发布版本,默认正式发布(默认开启)
    private static final String PK_B_SUPERPERMISSION_GRANTED = "PK_B_SUPERPERMISSION_GRANTED";//是否获取超级权限
    private static final String PK_B_FIRSTSTARTUP = "PK_B_FIRSTSTARTUP";//是否首次启动
    private static final String PK_S_FIRSTSTARTUP_DATETIME = "PK_S_FIRSTSTARTUP_DATETIME";//首次启动时间
    private static final String PK_S_APPSTARTUP_DATETIME_DAY = "PK_S_APPSTARTUP_DATETIME_DAY";//当天首次启动时间
    private static final String PK_S_APPUNIQUEID = "PK_S_APPUNIQUEID";//App唯一标识
    private static final String PK_S_APPUSERAGENT = "PK_S_APPUSERAGENT";//UserAgent
    private static final String PK_S_APPTERMINALID = "PREF_KEY_COMN_APP_TERMINAL_ID";//App唯一终端编号
    private static final String PK_B_CAMERASWEEP_ENABLED = "PK_B_CAMERASWEEP_ENABLED";//是否开启摄像头扫描
    private static final String PK_B_TTS_ENABLED    = "PK_B_TTS_ENABLED";//TTS语音播报(默认开启)
    private static final String PK_B_SOFTINPUT_ENABLED    = "PK_B_SOFTINPUT_ENABLED";//是否开启软键盘,默认开启
    private static final String PK_B_NOTIFICATION_ENABLED  = "PK_B_NOTIFICATION_ENABLED";//开启通知
    private static final String PK_B_LOCATION_ENABLED      = "PK_B_LOCATION_ENABLED";//开启位置服务

    /**
     * 是否正式版本，默认是true
     * */
    public static boolean isReleaseVersion(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_ISRELEASE, true);
    }

    public static void setReleaseVersion(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_ISRELEASE, enabled);
    }

    /**
     * 是否获取超级权限，默认是false
     * */
    public static boolean isSuperPermissionGranted(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_SUPERPERMISSION_GRANTED, false);
    }

    public static void setSuperPermissionGranted(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_SUPERPERMISSION_GRANTED, enabled);
    }

    public static void setAppFirstStart(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_FIRSTSTARTUP, enabled);
    }

    /**
     * 是否首次启动
     * */
    public static boolean isAppFirstStart(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_FIRSTSTARTUP, true);
    }

    /**
     * 首次启动时间
     * */
    public static String getAppStartupDateTime(){
        return getString(PREF_NAME_FACTORY, PK_S_FIRSTSTARTUP_DATETIME, "");
    }
    public static void setAppStartupDateTime(String dateTime){
        set(PREF_NAME_FACTORY, PK_S_FIRSTSTARTUP_DATETIME, dateTime);
    }
    /**
     * 当天首次启动时间
     * */
    public static String getAppDayFirstStartupDateTime(){
        return getString(PREF_NAME_FACTORY, PK_S_APPSTARTUP_DATETIME_DAY, "");
    }
    public static void setAppDayFirstStartupDateTime(String dateTime){
        set(PREF_NAME_FACTORY,  PK_S_APPSTARTUP_DATETIME_DAY, dateTime);
    }

    public static String getAppUniqueId(){
        return getString(PREF_NAME_FACTORY, PK_S_APPUNIQUEID, "");
    }
    public static void setAppUniqueId(String appId){
        set(SharedPrefesManagerFactory.PREF_NAME_FACTORY, PK_S_APPUNIQUEID, appId);
    }

    public static String getAppUserAgent(){
        return getString(PREF_NAME_FACTORY, PK_S_APPUSERAGENT, "");
    }
    public static void setAppUserAgent(String userAgent){
        set(SharedPrefesManagerFactory.PREF_NAME_FACTORY, PK_S_APPUSERAGENT, userAgent);
    }

    public static void setCameraSweepEnabled(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_CAMERASWEEP_ENABLED, enabled);
    }

    public static boolean isCameraSweepEnabled(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_CAMERASWEEP_ENABLED, false);
    }

    public static void setTtsEnabled(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_TTS_ENABLED, enabled);
    }

    public static boolean isTtsEnabled(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_TTS_ENABLED, false);
    }

    /**
     * 是否开启位置服务：默认true
     * */
    public static boolean getLocationAcceptEnabled(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_LOCATION_ENABLED, true);
    }


    public static void setLocationAcceptEnabled(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_LOCATION_ENABLED, enabled);
    }
    public static void setNotificationAcceptEnabled(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_NOTIFICATION_ENABLED, enabled);
    }

    /**
     * 是否接收通知：默认true
     * */
    public static boolean getNotificationAcceptEnabled(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_NOTIFICATION_ENABLED, true);
    }

    public static void setSoftInputEnabled(boolean enabled){
        set(PREF_NAME_FACTORY, PK_B_SOFTINPUT_ENABLED, enabled);
    }

    /**
     * 是否使用键盘
     * */
    public static boolean isSoftInputEnabled(){
        return getBoolean(PREF_NAME_FACTORY, PK_B_SOFTINPUT_ENABLED, true);
    }

    /**
     * 获取设备终端编号
     * */
    public static String getTerminalId(){
        return getString(PREF_NAME_FACTORY, PK_S_APPTERMINALID, "");
    }

    public static void setTerminalId(String id){
        set(PREF_NAME_FACTORY, PK_S_APPTERMINALID, id);
    }

    public static void clear(){
        clear(PREF_NAME_FACTORY);
    }
}
