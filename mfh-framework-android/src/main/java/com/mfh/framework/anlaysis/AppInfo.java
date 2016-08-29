package com.mfh.framework.anlaysis;

import java.io.Serializable;

/**
 * 应用信息
 * Created by bingshanguxue on 8/25/16.
 */
public class AppInfo implements Serializable{
    //应用名称
    private String appName = "";
    //应用包名
    private String packageName = "";
    //程序的版本信息
    private String versionName = "Unknown";
    //程序内部版本号
    private int versionCode = -1;
    //渠道名
    private String channelName = "Unknown";

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
