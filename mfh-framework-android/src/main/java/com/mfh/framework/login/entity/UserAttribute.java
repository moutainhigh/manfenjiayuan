package com.mfh.framework.login.entity;

/**
 *
 "userAttribute": {
     "mobile": "15250065084",
     "guid": "245389",
     "humanid": 245389,
     "ownerId": null,
     "humanName": "冰珊孤雪",
     "headimage": "33c49435faec068197c4e06f0e5d4eb7.jpg"
 }
 * */
public class UserAttribute implements java.io.Serializable {

    private String mobile = "";
    private String guid = "";
    private String humanId = "";
    private String ownerId = "";
    private String humanName = "";
    private String headimage = "";
    private String sex = "";//1=女，0=男,-1=未知


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

    public String getHumanId() {
        return humanId;
    }

    public void setHumanId(String humanId) {
        this.humanId = humanId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getHeadimage() {
        return headimage;
    }

    public void setHeadimage(String headimage) {
        this.headimage = headimage;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}