package com.mfh.framework.api.companyInfo;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;

/**
 * 网点
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-9-6
 */
public class CompanyInfo implements ILongId, Serializable {
    private Long id;        //companyId，netid
    private String name = "";    //名称
    private Long tenantId;//租户编号
    private String contact;//
    private String mobilenumber;
    private String addr;//地址

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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
