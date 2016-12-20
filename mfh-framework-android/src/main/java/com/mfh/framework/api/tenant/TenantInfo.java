package com.mfh.framework.api.tenant;

import java.io.Serializable;

/**
 * 生态租户信息
 * Created by bingshanguxue on 16/12/2016.
 */

public class TenantInfo implements Serializable{
    private Long saasId;//租户编号
    private String saasName;//租户名称
    private String area;//城市
    private String id;//域名
    private int domainUrlType;//域名地址类型
    private int bizDomainType;//域名业务类型

    public Long getSaasId() {
        return saasId;
    }

    public void setSaasId(Long saasId) {
        this.saasId = saasId;
    }

    public String getSaasName() {
        return saasName;
    }

    public void setSaasName(String saasName) {
        this.saasName = saasName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
