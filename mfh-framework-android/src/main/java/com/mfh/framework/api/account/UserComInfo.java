/*
 * 文件名称: UserComInfo.java
 * 版权信息: Copyright 2013-2015 chunchen technology Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: zhangyz
 * 修改日期: 2015-1-21
 * 修改内容: 
 */
package com.mfh.framework.api.account;

import java.util.List;

/**
 * 公司及其授权的相关信息
 * @author zhangyz created on 2015-1-21
 */
@SuppressWarnings("serial")
public class UserComInfo implements java.io.Serializable {
    
    private String pmcName = "";//所属公司名称    
    private Long spid;//所属公司编号

    private List<Office> offices = null;//部门列表
    private Long curOffice;//当前所属部门编号
    private String logopic = "";
    private String pmcLevel = "";//复用：物业公司级别/或公司所带仓储编号
    private String subdisIds = "";//当前用户所管辖的小区列表
    private List<Subdis> subdisList = null;//当前用户所管辖的小区列表信息。
    private String moduleNames;//拥有的功能列表

    public Long getSpid() {
        return spid;
    }

    public void setSpid(Long spid) {
        this.spid = spid;
    }

    public String getModuleNames() {
        return moduleNames;
    }
    
    public void setModuleNames(String moduleNames) {
        this.moduleNames = moduleNames;
    }


    public String getLogopic() {
        return logopic;
    }

    public void setLogopic(String logopic) {
        this.logopic = logopic;
    }

    public String getPmcName() {
        return pmcName;
    }

    public void setPmcName(String pmcName) {
        this.pmcName = pmcName;
    }

    public String getSubdisIds() {
        return subdisIds;
    }

    public void setSubdisIds(String subdisIds) {
        this.subdisIds = subdisIds;
    }

    public String getPmcLevel() {
        return pmcLevel;
    }

    public void setPmcLevel(String pmcLevel) {
        this.pmcLevel = pmcLevel;
    }

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    public Long getCurOffice() {
        return curOffice;
    }

    public void setCurOffice(Long curOffice) {
        this.curOffice = curOffice;
    }

    public List<Subdis> getSubdisList() {
        return subdisList;
    }

    public void setSubdisList(List<Subdis> subdisList) {
        this.subdisList = subdisList;
    }

}
