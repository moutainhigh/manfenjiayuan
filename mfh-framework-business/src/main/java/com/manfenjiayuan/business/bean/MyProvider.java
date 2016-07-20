package com.manfenjiayuan.business.bean;

import com.mfh.framework.api.CashierApi;

/**
 * Created by bingshanguxue on 5/29/16.
 */
public class MyProvider {
    private Provider provider;
    private String name;
    /**
     * {@link CashierApi#URL_INVCOMPPROVIDER_FINDMYPROVIDERS sendTenantId}*/
    private Long id;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
