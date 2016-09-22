package com.mfh.framework.api.posRegister;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * Created by bingshanguxue on 9/20/16.
 */

public class PosRegister extends MfhEntity<Long> {

    private Long channelId;
    private String channelPointId;
    private Long netId;
    private String serialNo;
    private String appVersion;
    private String deviceNo;
    private Integer status;

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelPointId() {
        return channelPointId;
    }

    public void setChannelPointId(String channelPointId) {
        this.channelPointId = channelPointId;
    }

    public Long getNetId() {
        return netId;
    }

    public void setNetId(Long netId) {
        this.netId = netId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
