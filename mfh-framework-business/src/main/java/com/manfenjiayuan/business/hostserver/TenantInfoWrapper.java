package com.manfenjiayuan.business.hostserver;

import com.manfenjiayuan.business.R;
import com.mfh.framework.api.tenant.PayCfgId;

import java.io.Serializable;
import java.util.List;

/**
 * 租户
 * Created by bingshanguxue on 04/11/2016.
 */

public class TenantInfoWrapper implements Serializable {
    private Long saasId;//租户编号
    private String saasName;//租户名称
    private String area;//城市编号
    private String domainUrl;// 域名地址

    private String contact; //联系人
    private String mobilenumber; //手机号码
    private String logopicUrl;// 完整的公司头像url,对应logopic
    private List<PayCfgId> payInfos;//256-微信扫码付; 512-微信app支付 ；4096-支付宝app支付； 2-支付宝扫码付

//    private String baseServerUrl;


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

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

//    public String getBaseServerUrl() {
//        return baseServerUrl;
//    }
//
//    public void setBaseServerUrl(String baseServerUrl) {
//        this.baseServerUrl = baseServerUrl;
//    }

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

    public List<PayCfgId> getPayInfos() {
        return payInfos;
    }

    public void setPayInfos(List<PayCfgId> payInfos) {
        this.payInfos = payInfos;
    }


    /**
     * 根据租户编号获取对应的logo图标
     * */
    public static int getImageResource(Long saasId){
        if (saasId != null) {
            if (saasId.equals(134342L)){
                return R.mipmap.ic_launcher_mixicook;
            }
            else if (saasId.equals(137039L)){
                return R.mipmap.ic_launcher_lanlj;
            }
            else if (saasId.equals(137143L)){
                return R.mipmap.ic_launcher_qianwj;
            }
            else if (saasId.equals(137540L)){
                return R.mipmap.ic_launcher_banmx;
            }
            else if (saasId.equals(130222L)){
                return R.mipmap.ic_launcher_mixicook;
            }
        }

        return R.mipmap.ic_launcher;
    }
}
