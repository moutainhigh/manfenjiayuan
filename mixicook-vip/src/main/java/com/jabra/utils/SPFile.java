package com.jabra.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashSet;
import java.util.Set;

public class SPFile {
    private SharedPreferences sp;

    public SPFile(Context paramContext, String paramString) {
        this.sp = paramContext.getSharedPreferences(paramString, 0);
    }

    public void addString2Set(String paramString1, String paramString2) {
        Object localObject = getStringSet(paramString1);
        if (localObject == null) {
            localObject = new HashSet();
        }
        ((Set) localObject).add(paramString2);
        putStringSet(paramString1, (Set) localObject);
    }

    public void clear() {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.clear();
        localEditor.commit();
    }

    public boolean getBoolean(String paramString, boolean paramBoolean) {
        return this.sp.getBoolean(paramString, paramBoolean);
    }

    public int getInt(String paramString, int paramInt) {
        return this.sp.getInt(paramString, paramInt);
    }

    public long getLong(String paramString, long paramLong) {
        return this.sp.getLong(paramString, paramLong);
    }

    public String getString(String paramString1, String paramString2) {
        return this.sp.getString(paramString1, paramString2);
    }

    @SuppressLint({"NewApi"})
    public Set<String> getStringSet(String paramString) {
        return this.sp.getStringSet(paramString, null);
    }

    public void put(String paramString, int paramInt) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.putInt(paramString, paramInt);
        localEditor.commit();
    }

    public void put(String paramString, long paramLong) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.putLong(paramString, paramLong);
        localEditor.commit();
    }

    public void put(String paramString1, String paramString2) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.putString(paramString1, paramString2);
        localEditor.commit();
    }

    public void put(String paramString, boolean paramBoolean) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.putBoolean(paramString, paramBoolean);
        localEditor.commit();
    }

    @SuppressLint({"NewApi"})
    public void putStringSet(String paramString, Set<String> paramSet) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.remove(paramString);
        localEditor.commit();
        localEditor.putStringSet(paramString, paramSet);
        localEditor.commit();
    }

    public void remove(String paramString) {
        SharedPreferences.Editor localEditor = this.sp.edit();
        localEditor.remove(paramString);
        localEditor.commit();
    }
}
