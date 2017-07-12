package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

import java.util.Date;

/**
 * 团购活动
 * Created by bingshanguxue on 03/07/2017.
 */

public class GroupBuyActivity extends MfhEntity<Long> {
    private Long unTakeHuman;
    private Double totalMoney;
    private String imgUrl;
    private String activityTitle;
    private String activitySubTitle;
    private Integer activityImage;
    private Integer activityStatus;
    private Date gropuEndDate;
    private Date takeGoodsDate;
    private Long joinHumanNum;
    private Long tenantId;
    private Long sassId;

    public Long getUnTakeHuman() {
        return unTakeHuman;
    }

    public void setUnTakeHuman(Long unTakeHuman) {
        this.unTakeHuman = unTakeHuman;
    }

    public Double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(Double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivitySubTitle() {
        return activitySubTitle;
    }

    public void setActivitySubTitle(String activitySubTitle) {
        this.activitySubTitle = activitySubTitle;
    }

    public Integer getActivityImage() {
        return activityImage;
    }

    public void setActivityImage(Integer activityImage) {
        this.activityImage = activityImage;
    }

    public Integer getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(Integer activityStatus) {
        this.activityStatus = activityStatus;
    }

    public Date getGropuEndDate() {
        return gropuEndDate;
    }

    public void setGropuEndDate(Date gropuEndDate) {
        this.gropuEndDate = gropuEndDate;
    }

    public Date getTakeGoodsDate() {
        return takeGoodsDate;
    }

    public void setTakeGoodsDate(Date takeGoodsDate) {
        this.takeGoodsDate = takeGoodsDate;
    }

    public Long getJoinHumanNum() {
        return joinHumanNum;
    }

    public void setJoinHumanNum(Long joinHumanNum) {
        this.joinHumanNum = joinHumanNum;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getSassId() {
        return sassId;
    }

    public void setSassId(Long sassId) {
        this.sassId = sassId;
    }
}
