package com.manfenjiayuan.business.bean.wrapper;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 04/11/2016.
 */

public class HostServer implements Serializable{
    private Long id;
    private String name;
    private String host;
    private String baseServerUrl;
    private String baseMessageUrl;

    private int resId;

    public HostServer() {
    }

    public HostServer(Long id, String name, String host, String baseServerUrl, String baseMessageUrl, int resId) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.baseServerUrl = baseServerUrl;
        this.baseMessageUrl = baseMessageUrl;
        this.resId = resId;
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

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
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

    public void setBaseServerUrl(String baseServerUrl) {
        this.baseServerUrl = baseServerUrl;
    }

    public String getBaseMessageUrl() {
        return baseMessageUrl;
    }

    public void setBaseMessageUrl(String baseMessageUrl) {
        this.baseMessageUrl = baseMessageUrl;
    }
}
