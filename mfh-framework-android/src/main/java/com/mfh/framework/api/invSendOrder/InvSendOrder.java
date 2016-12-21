package com.mfh.framework.api.invSendOrder;

import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.invOrder.InvOrderApi;

/**
 * 采购单
 * Created by bingshanguxue on 15/9/22.
 */
public class InvSendOrder extends MfhEntity<Long> {
//    private Long id;//订单编号
    private Double goodsFee = 0D;//商品金额
//    private Double transFee = 0D;//配送费
//    private Double totalFee = 0D;//总金额
    //订单状态:0-"已下单",1-"待发货",2-"配送中",3-"已到达",4-"已签收",100-"已经取消"
    private Integer status = InvOrderApi.ORDER_STATUS_INIT;
    private String statusCaption;//状态
    private String contact;//联系人
    private String receiveMobile;//电话
    private String name;//订单名称
    private Double askTotalCount;//订单商品总数

    //发货方信息
    private Long sendNetId;
    private Long sendTenantId;//发货网点
    private String sendCompanyName;//发货方供应商名称
    private Integer isPrivate;//是否是私有供应商



    public Double getGoodsFee() {
        return goodsFee;
    }

    public void setGoodsFee(Double goodsFee) {
        this.goodsFee = goodsFee;
    }

//    public Double getTransFee() {
//        return transFee;
//    }
//
//    public void setTransFee(Double transFee) {
//        this.transFee = transFee;
//    }
//
//    public Double getTotalFee() {
//        return totalFee;
//    }
//
//    public void setTotalFee(Double totalFee) {
//        this.totalFee = totalFee;
//    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getReceiveMobile() {
        return receiveMobile;
    }

    public void setReceiveMobile(String receiveMobile) {
        this.receiveMobile = receiveMobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSendCompanyName() {
        return sendCompanyName;
    }

    public void setSendCompanyName(String sendCompanyName) {
        this.sendCompanyName = sendCompanyName;
    }

    public Double getAskTotalCount() {
        return askTotalCount;
    }

    public void setAskTotalCount(Double askTotalCount) {
        this.askTotalCount = askTotalCount;
    }

    public String getStatusCaption() {
        return statusCaption;
    }

    public void setStatusCaption(String statusCaption) {
        this.statusCaption = statusCaption;
    }

    public Long getSendNetId() {
        return sendNetId;
    }

    public void setSendNetId(Long sendNetId) {
        this.sendNetId = sendNetId;
    }

    public Long getSendTenantId() {
        return sendTenantId;
    }

    public void setSendTenantId(Long sendTenantId) {
        this.sendTenantId = sendTenantId;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }
}
