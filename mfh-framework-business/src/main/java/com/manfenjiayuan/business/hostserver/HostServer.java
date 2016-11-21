package com.manfenjiayuan.business.hostserver;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 04/11/2016.
 */

public class HostServer implements Serializable {
    private Long id;
    private String name;
    private String host;
    private String baseServerUrl;

    private int textLogoResId;
    private int imgLogoResId;
    private String activityAlias;

    private String skinName;


    public HostServer() {
    }

    public HostServer(Long id, String name,
                      String host, String baseServerUrl,
                      int textLogoResId, int imgLogoResId,
                      String activityAlias, String skinName) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.baseServerUrl = baseServerUrl;
        this.textLogoResId = textLogoResId;
        this.imgLogoResId = imgLogoResId;
        this.activityAlias = activityAlias;
        this.skinName = skinName;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTextLogoResId() {
        return textLogoResId;
    }

    public void setTextLogoResId(int textLogoResId) {
        this.textLogoResId = textLogoResId;
    }

    public int getImgLogoResId() {
        return imgLogoResId;
    }

    public void setImgLogoResId(int imgLogoResId) {
        this.imgLogoResId = imgLogoResId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBaseServerUrl() {
        return baseServerUrl;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setSkinName(String skinName) {
        this.skinName = skinName;
    }

    public void setBaseServerUrl(String baseServerUrl) {
        this.baseServerUrl = baseServerUrl;
    }

    public String getActivityAlias() {
        return activityAlias;
    }

    public void setActivityAlias(String activityAlias) {
        this.activityAlias = activityAlias;
    }
}
