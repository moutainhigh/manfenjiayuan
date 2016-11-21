package com.manfenjiayuan.business.utils;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesUltimate;

/**
 * 应用业务层配置和登录用户无关
 * Created by bingshanguxue on 10/11/2016.
 */

public class SharedPrefesManagerBase extends SharedPrefesUltimate {
    public static String PREF_NAME_APP = "pref_app";

    private static final String PK_S_HOSTSERVER = "PK_S_HOSTSERVER";//租户

    //    private static SharedPreferencesHelper instance = null;
//    /**
//     * 返回 SharedPreferencesHelper 实例
//     * @return
//     */
//    public static SharedPreferencesHelper getInstance() {
//
//        if (instance == null) {
//            synchronized (SharedPreferencesHelper.class) {
//                if (instance == null) {
//                    instance = new SharedPreferencesHelper();
//                }
//            }
//        }
//        return instance;
//    }

    public static HostServer getHostServer(){
        String jsonData = getText(SharedPrefesManagerBase.PK_S_HOSTSERVER, null);
        if (StringUtils.isEmpty(jsonData)){
            return null;
        }
        return JSONObject.toJavaObject(JSONObject.parseObject(jsonData), HostServer.class);
    }

    public static void setHostServer(HostServer hostServer){
        if (hostServer != null){
            set(SharedPrefesManagerBase.PK_S_HOSTSERVER, JSONObject.toJSONString(hostServer));
        }
        else{
            set(SharedPrefesManagerBase.PK_S_HOSTSERVER, "");
        }
    }


    public static String getText(String key) {
        return getString(PREF_NAME_APP, key, "");
    }

    public static String getText(String key, String defVal) {
        return getString(PREF_NAME_APP, key, defVal);
    }

    public static int getInt(String key, int defVal) {
        return getInt(PREF_NAME_APP, key, defVal);
    }

    public static Long getLong(String key, Long defVal) {
        return getLong(PREF_NAME_APP, key, defVal);
    }
    public static boolean getBoolean(String key, boolean defVal) {
        return getBoolean(PREF_NAME_APP, key, defVal);
    }

    public static void set(String key, String value) {
        set(PREF_NAME_APP, key, value);
    }
    public static void set(String key, int value) {
        set(PREF_NAME_APP, key, value);
    }
    public static void set(String key, Long value) {
        set(PREF_NAME_APP, key, value);
    }
    public static void set(String key, boolean value) {
        set(PREF_NAME_APP, key, value);
    }

}
