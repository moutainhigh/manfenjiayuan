/*
 * 文件名称: PmcUser.java
 * 版权信息: Copyright 2013-2014 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: zhangyz
 * 修改日期: 2014-3-14
 * 修改内容: 
 */
package com.mfh.framework.api.account;

import com.mfh.framework.api.constant.HumanAuthFlag;

import java.util.ArrayList;
import java.util.List;


/**
 * 用户信息(调用登录接口返回)
 * @author zhangyz created on 2014-3-14
 */
@SuppressWarnings("serial")
public class UserMixInfo extends User {
    private UserAttribute userAttribute;//各类用户混合信息
    private String sessionId;//会话Id
    private Integer humanAuthFlag = HumanAuthFlag.NA;//认证标志
    private List<UserComInfo> comInfos = null;//公司相关信息
    private List<String> cookiees; //仅返回给手机客户端的cookiees
    //menus

    public UserAttribute getUserAttribute() {
        return userAttribute;
    }

    public void setUserAttribute(UserAttribute userAttribute) {
        this.userAttribute = userAttribute;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getCookiees() {
        return cookiees;
    }

    /**
     * 增加一个cookiee
     * @param cookiee
     * @author zhangyz created on 2015-1-18
     */
    public void addCookiess(String cookiee) {
        cookiees = new ArrayList<String>();
        cookiees.add(cookiee);
    }

    public void setCookiees(List<String> cookiees) {
        this.cookiees = cookiees;
    }




    public Integer getHumanAuthFlag() {
        return humanAuthFlag;
    }

    public void setHumanAuthFlag(Integer humanAuthFlag) {
        this.humanAuthFlag = humanAuthFlag;
    }

    public List<UserComInfo> getComInfos() {
        return comInfos;
    }

    public void setComInfos(List<UserComInfo> comInfos) {
        this.comInfos = comInfos;
    }

    /**
     * 增加一个公司
     * @param com
     * @author zhangyz created on 2015-1-21
     */
    public  void addComInfo(UserComInfo com) {
        if (comInfos == null)
            comInfos = new ArrayList<UserComInfo>();
        comInfos.add(com);
    }

}
