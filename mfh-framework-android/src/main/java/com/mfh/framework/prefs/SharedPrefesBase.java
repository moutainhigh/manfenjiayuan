package com.mfh.framework.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bingshanguxue on 2015/5/26.
 */
public class SharedPrefesBase {
    public static SharedPreferences.Editor getEditor(Context context, String prefName) {
        SharedPreferences preferences = getSharedPreferences(context, prefName);
        return preferences.edit();
    }

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

    public static void clear(Context context, String prefName){
        SharedPreferences.Editor editor = getEditor(context, prefName);
        editor.clear();
        editor.commit();
    }

    public static SharedPreferences getSharedPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName,
                Context.MODE_PRIVATE);//MODE_MULTI_PROCESS;
    }

    public static boolean getBoolean(Context context, String prefName, String key, boolean defValue) {
        return getSharedPreferences(context, prefName).getBoolean(key, defValue);
    }

    public static String getString(Context context, String prefName, String key, String defValue) {
        return getSharedPreferences(context, prefName).getString(key, defValue);
    }

    public static Long getLong(Context context, String prefName, String key, Long defValue) {
        return getSharedPreferences(context, prefName).getLong(key, defValue);
    }

    public static int getInt(Context context, String prefName, String key, int defValue) {
        return getSharedPreferences(context, prefName).getInt(key, defValue);
    }


    public static void set(SharedPreferences.Editor editor, String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void set(SharedPreferences.Editor editor, String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public static void set(SharedPreferences.Editor editor, String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public static void set(SharedPreferences.Editor editor, String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public static void clear(SharedPreferences.Editor editor){
        editor.clear();
        editor.commit();
    }

    public static boolean getBoolean(SharedPreferences preferences, String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public static String getString(SharedPreferences preferences, String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public static Long getLong(SharedPreferences preferences, String key, Long defValue) {
        return preferences.getLong(key, defValue);
    }

    public static int getInt(SharedPreferences preferences, String key, int defValue) {
        return preferences.getInt(key, defValue);
    }



}
