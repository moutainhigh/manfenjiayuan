package com.mfh.framework.api.invSendIoOrder;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.invOrder.InvOrderApi;

import java.io.Serializable;
import java.util.Date;

/**
 * 收发单：采购收货单/采购退货单/调拨单
 * Created by bingshanguxue on 15/9/22.
 */
public class InvSendIoOrder implements ILongId, Serializable {
    private Long id;//订单编号
    private String orderName;//订单名称
    private Double commitPrice = 0D;//商品金额
    private Double commitGoodsNum;//订单商品总数
    //订单状态:0-"已下单",1-"待发货",2-"配送中",3-"已到达",4-"已签收",100-"已经取消"
    private Integer status;
    private String statusCaption;//状态
    private String receiveMobile;//电话
    private Date createdDate;//下单时间

    //支付状态，0-未支付，1-已支付
    private Integer payStatus = InvOrderApi.PAY_STATUS_NOT_PAID;

    private Long sendTenantId;//发货网点
    private String sendCompanyName;//发货方－供应商

    //以下是采购收货单特有属性
    private String contact;//收货方联系人
    private Long sendNetId;//发货网点
    private String sendNetName;//发货网点名称

    //以下是采购退货单特有属性
    private String auditHumanName;//经手人
    private String receiveNetName;//收货网点名称


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Double getCommitPrice() {
        if (commitPrice == null){
            return 0D;
        }
        return commitPrice;
    }

    public void setCommitPrice(Double commitPrice) {
        this.commitPrice = commitPrice;
    }

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

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getSendCompanyName() {
        return sendCompanyName;
    }

    public void setSendCompanyName(String sendCompanyName) {
        this.sendCompanyName = sendCompanyName;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public String getSendNetName() {
        return sendNetName;
    }

    public void setSendNetName(String sendNetName) {
        this.sendNetName = sendNetName;
    }

    public Long getSendTenantId() {
        return sendTenantId;
    }

    public void setSendTenantId(Long sendTenantId) {
        this.sendTenantId = sendTenantId;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public String getAuditHumanName() {
        return auditHumanName;
    }

    public void setAuditHumanName(String auditHumanName) {
        this.auditHumanName = auditHumanName;
    }

    public String getReceiveNetName() {
        return receiveNetName;
    }

    public void setReceiveNetName(String receiveNetName) {
        this.receiveNetName = receiveNetName;
    }
}
