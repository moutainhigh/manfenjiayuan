package com.mfh.litecashier.bean;/*
 * 文件名称: PosGoods.java
 * 版权信息: Copyright 2013-2015 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: zhangyz
 * 修改日期: 2015-9-6
 * 修改内容:
 */

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工
 *
 * @author zhangyz created on 2015-9-6
 */
public class CompanyHuman implements ILongId, Serializable {
//    {
//        "id": 131291,
//            "name": "张佩",
//            "userId": 937,
//            "companyId": 130222,
//            "userName": "18068423112",
//            "password": "42e2d75f31edde9f8efc8c427fd91cb4be78a0e0",
//            "salt": "7bdd025c4b27266b",
//            "updatedDate": "2015-07-19 12:13:26"
//    }
    private Long id;//humanId
    private String name; //昵称
    private Long userId;//用户编号
    private Long companyId;//公司编号
    private String userName; // 登录用户名
    private String password; // 登录密码
    private String salt; // 登录密码
    private Date updatedDate;


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

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
