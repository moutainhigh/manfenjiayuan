package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;

import java.util.List;

import me.drakeet.multitype.Item;

/**
 * 团购活动订单
 * Created by bingshanguxue on 03/07/2017.
 */

public class GroupBuyOrder extends MfhEntity<Long> implements Item {
    private Long activityId;
    private String buyerName;
    private String receivePhone;
    private Double bcount;
    private Integer status;
    private Integer btype;
    private String btypename;
    private List<GroupBuyOrderItem> items;

    private boolean selected;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getReceivePhone() {
        return receivePhone;
    }

    public void setReceivePhone(String receivePhone) {
        this.receivePhone = receivePhone;
    }

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBtype() {
        return btype;
    }

    public void setBtype(Integer btype) {
        this.btype = btype;
    }

    public String getBtypename() {
        return btypename;
    }

    public void setBtypename(String btypename) {
        this.btypename = btypename;
    }

    public List<GroupBuyOrderItem> getItems() {
        return items;
    }

    public void setItems(List<GroupBuyOrderItem> items) {
        this.items = items;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
