package com.manfenjiayuan.mixicook_vip.ui.order;

import java.io.Serializable;

/**
 * 预支付订单
 * Created by bingshanguxue on 02/11/2016.
 */

public class PrePayOrderInfo implements Serializable{
    private Long preOrderId;
    private String orderIds;
    private Integer btype;
    private String token;
    private Integer wayType;

    public Long getPreOrderId() {
        return preOrderId;
    }

    public void setPreOrderId(Long preOrderId) {
        this.preOrderId = preOrderId;
    }

    public String getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }

    public Integer getBtype() {
        return btype;
    }

    public void setBtype(Integer btype) {
        this.btype = btype;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getWayType() {
        return wayType;
    }

    public void setWayType(Integer wayType) {
        this.wayType = wayType;
    }
}
