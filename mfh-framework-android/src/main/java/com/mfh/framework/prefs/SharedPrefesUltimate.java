package com.mfh.framework.prefs;

import android.content.SharedPreferences;

import com.mfh.framework.MfhApplication;

/**
 * Created by bingshanguxue on 15/11/2016.
 */

public class SharedPrefesUltimate extends SharedPrefesBase {
    public static SharedPreferences.Editor getDefaultEditor(String prefName) {
        return getEditor(MfhApplication.getAppContext(), prefName);
    }

    public static void set(String prefName, String key, String value) {
        set(getDefaultEditor(prefName), key, value);
    }
    public static void set(String prefName, String key, int value) {
        set(getDefaultEditor(prefName), key, value);
    }
    public static void set(String prefName, String key, Long value) {
        set(getDefaultEditor(prefName), key, value);
    }
    public static void set(String prefName, String key, boolean value) {
        set(getDefaultEditor(prefName), key, value);
    }

    public static void clear(String prefName){
        clear(getDefaultEditor(prefName));
    }

    public static SharedPreferences getDefaultPreferences(String prefName) {
        return getSharedPreferences(MfhApplication.getAppContext(), prefName);
    }

    public static String getString(String prefName, String key) {
        return getString(getDefaultPreferences(prefName), key, "");
    }

    public static String getString(String prefName, String key, String defVal) {
        return getString(getDefaultPreferences(prefName), key, defVal);
    }

    public static int getInt(String prefName, String key, int defVal) {
        return getInt(getDefaultPreferences(prefName), key, defVal);
    }

    public static Long getLong(String prefName, String key, Long defVal) {
        return getLong(getDefaultPreferences(prefName), key, defVal);
    }
    public static boolean getBoolean(String prefName, String key, boolean defVal) {
        return getBoolean(getDefaultPreferences(prefName), key, defVal);
    }

}
