package com.mfh.framework.api.invCompProvider;

import com.mfh.framework.api.cashier.CashierApi;

import java.io.Serializable;
import java.security.Provider;


/**
 * Created by bingshanguxue on 5/29/16.
 */
public class MyProvider implements Serializable{
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
