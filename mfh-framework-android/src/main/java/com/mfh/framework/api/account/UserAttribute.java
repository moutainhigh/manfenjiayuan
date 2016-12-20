package com.mfh.framework.api.account;

import com.mfh.framework.api.constant.Sex;

/**
 * 各类用户混合信息
 * */
public class UserAttribute implements java.io.Serializable {

    private String mobile = "";
    private String guid = "";
    private Long humanId;
    private Long ownerId;
    private String humanName = "";
    private String headimage = "";
    private Integer sex = Sex.UNKNOWN;


    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Long getHumanId() {
        return humanId;
    }

    public void setHumanId(Long humanId) {
        this.humanId = humanId;
    }

    public Long getOwnerId() {
        if (ownerId == null){
            return 0L;
        }
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getHeadimage() {
        return headimage;
    }

    public void setHeadimage(String headimage) {
        this.headimage = headimage;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }
}