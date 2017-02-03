package com.mfh.framework.api.invIoOrder;

import com.mfh.framework.api.abs.MfhEntity;

/**
 *  批次流水
 * Created by bingshanguxue on 15/9/22.
 */
public class InvIoOrder extends MfhEntity<Long> {
//    private Long id;//订单编号
    private String orderName;//订单名称
    private String statusCaption;//状态
    private String bizTypeCaption;//业务类型
    private String storeTypeCaption;//仓库类型
    private String netName;//网点名称
    private Double commitGoodsNum;//商品数量


    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getStatusCaption() {
        return statusCaption;
    }

    public void setStatusCaption(String statusCaption) {
        this.statusCaption = statusCaption;
    }

    public String getBizTypeCaption() {
        return bizTypeCaption;
    }

    public void setBizTypeCaption(String bizTypeCaption) {
        this.bizTypeCaption = bizTypeCaption;
    }

    public String getStoreTypeCaption() {
        return storeTypeCaption;
    }

    public void setStoreTypeCaption(String storeTypeCaption) {
        this.storeTypeCaption = storeTypeCaption;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public Double getCommitGoodsNum() {
        if (commitGoodsNum == null){
            return 0D;
        }
        return commitGoodsNum;
    }

    public void setCommitGoodsNum(Double commitGoodsNum) {
        this.commitGoodsNum = commitGoodsNum;
    }
}
