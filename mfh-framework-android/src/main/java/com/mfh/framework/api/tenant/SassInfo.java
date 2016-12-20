package com.mfh.framework.api.tenant;

import java.io.Serializable;
import java.util.List;

/**
 * 生态租户的详细信息
 * Created by bingshanguxue on 16/12/2016.
 */

public class SassInfo implements Serializable {
    private Long id;
    private String name; //商家名称
    private String contact; //联系人
    private String mobilenumber; //手机号码
    private String logopicUrl;// 完整的公司头像url,对应logopic

    private String domainUrl;//域名

    private List<PayCfgId> payInfos;//256-微信扫码付; 512-微信app支付 ；4096-支付宝app支付； 2-支付宝扫码付

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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getLogopicUrl() {
        return logopicUrl;
    }

    public void setLogopicUrl(String logopicUrl) {
        this.logopicUrl = logopicUrl;
    }

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public List<PayCfgId> getPayInfos() {
        return payInfos;
    }

    public void setPayInfos(List<PayCfgId> payInfos) {
        this.payInfos = payInfos;
    }
}
