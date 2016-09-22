package com.mfh.framework.api.clientLog;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by bingshanguxue on 9/22/16.
 */

public class ClientLog implements Serializable{
    private Long id;
    private String ip;
    private String stackInformation; //堆栈信息
    private String hardwareInformation; //硬件版本信息
    private String androidLevel; //安卓版本号
    private String loginName; //登录名
    private String softVersion; //程序的版本号
    private Date errorTime; //错误时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStackInformation() {
        return stackInformation;
    }

    public void setStackInformation(String stackInformation) {
        this.stackInformation = stackInformation;
    }

    public String getHardwareInformation() {
        return hardwareInformation;
    }

    public void setHardwareInformation(String hardwareInformation) {
        this.hardwareInformation = hardwareInformation;
    }

    public String getAndroidLevel() {
        return androidLevel;
    }

    public void setAndroidLevel(String androidLevel) {
        this.androidLevel = androidLevel;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getSoftVersion() {
        return softVersion;
    }

    public void setSoftVersion(String softVersion) {
        this.softVersion = softVersion;
    }

    public Date getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(Date errorTime) {
        this.errorTime = errorTime;
    }
}
