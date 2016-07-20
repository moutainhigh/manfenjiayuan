package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;
import java.util.Date;

/**
 * 调拨单
 * Created by Nat.ZZN(bingshanguxue) on 15/9/22.
 */
public class InvTransOrder implements ILongId, Serializable {
    private Long id;//订单编号
    private String orderName;//订单名称
    private Double commitPrice;//商品金额
//    private Double transFee;//配送费
//    private Double totalFee;//总金额
    //订单状态:0-"已下单",1-"待发货",2-"配送中",3-"已到达",4-"已签收",100-"已经取消"
    private Integer status;
    private String statusCaption;//状态
    private String contact;//收货方联系人
    private String receiveMobile;//电话
    private String sendCompanyName;//发货方－供应商
    private Double commitGoodsNum;//订单商品总数
    private Date createdDate;//下单时间
    //    private Long receiveNetId;//收货网点
    private String receiveNetName;//调入网点名称
//    private Long tenantId;//收货网点所属租户
//    private Long sendNetId;//发货网点
    private String auditHumanName;//发货放联系人
    private Long sendTenantId;//发货网点所属租户
    private String sendNetName;//调出网点名称
    //支付状态，0-未支付，1-已支付
    private Integer payStatus;

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

//    public Double getTransFee() {
//        if (transFee == null){
//            return 0D;
//        }
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

    public String getReceiveNetName() {
        return receiveNetName;
    }

    public void setReceiveNetName(String receiveNetName) {
        this.receiveNetName = receiveNetName;
    }

    public String getSendNetName() {
        return sendNetName;
    }

    public void setSendNetName(String sendNetName) {
        this.sendNetName = sendNetName;
    }

    public String getAuditHumanName() {
        return auditHumanName;
    }

    public void setAuditHumanName(String auditHumanName) {
        this.auditHumanName = auditHumanName;
    }
}
