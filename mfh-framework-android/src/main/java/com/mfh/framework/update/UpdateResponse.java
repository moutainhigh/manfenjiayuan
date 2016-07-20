package com.mfh.framework.update;

import java.io.Serializable;

/**
 * 应用程序信息
 * Created by Administrator on 14-6-4.
 */
public class UpdateResponse implements Serializable {
    private String versionName;
    private int versionCode;
    private String apkName;
    private String updateLog;

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

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }
}

