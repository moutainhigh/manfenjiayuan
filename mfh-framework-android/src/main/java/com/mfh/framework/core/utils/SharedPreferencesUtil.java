package com.mfh.framework.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bingshanguxue on 2015/5/26.
 */
public class SharedPreferencesUtil {

    public static void set(Context context, String prefName, String key, boolean value) {
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void set(Context context, String prefName, String key, String value) {
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.putString(key, value);
        editor.commit();
    }

    public static void set(Context context, String prefName, String key, int value) {
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.putInt(key, value);
        editor.commit();
    }

    public static void set(Context context, String prefName, String key, long value) {
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.putLong(key, value);
        editor.commit();
    }


    public static boolean get(Context context, String prefName, String key, boolean defValue) {
        return getSharedPreferences(context, prefName).getBoolean(key, defValue);
    }

    public static String get(Context context, String prefName, String key, String defValue) {
        return getSharedPreferences(context, prefName).getString(key, defValue);
    }

    public static Long getLong(Context context, String prefName, String key, Long defValue) {
        return getSharedPreferences(context, prefName).getLong(key, defValue);
    }

    public static int getInt(Context context, String prefName, String key, int defValue) {
        return getSharedPreferences(context, prefName).getInt(key, defValue);
    }

    public static void clear(Context context, String prefName){
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.clear();
        editor.commit();
    }

    public static SharedPreferences getSharedPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName,
                Context.MODE_PRIVATE);//MODE_MULTI_PROCESS;
    }

    public static SharedPreferences.Editor getEditor(Context context, String prefName) {
        SharedPreferences preferences = getSharedPreferences(context, prefName);
        return preferences.edit();
    }
}
