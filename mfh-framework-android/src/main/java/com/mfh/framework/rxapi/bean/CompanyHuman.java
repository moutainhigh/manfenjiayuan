package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

/**
 * 员工账号
 *
 * @author zhangyz created on 2015-9-6
 */
public class CompanyHuman extends MfhEntity<Long> {
//    private Long id;//humanId
    private String name; //昵称
    private Long userId;//用户编号
    private Long companyId;//公司编号
    private String userName; // 登录用户名
    private String password; // 登录密码
    private String salt; // 登录密码
    private Long privateHumanId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Long getPrivateHumanId() {
        return privateHumanId;
    }

    public void setPrivateHumanId(Long privateHumanId) {
        this.privateHumanId = privateHumanId;
    }
}
