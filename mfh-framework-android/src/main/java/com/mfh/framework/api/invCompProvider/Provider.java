package com.mfh.framework.api.invCompProvider;

/**
 * 供应商
 * Created by bingshanguxue on 5/29/16.
 */
public class Provider {
    private Long companyId;
    private Long providerId;
    private String providerName;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
