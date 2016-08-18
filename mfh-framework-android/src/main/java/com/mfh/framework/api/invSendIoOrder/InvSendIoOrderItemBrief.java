package com.mfh.framework.api.invSendIoOrder;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.constant.IsPrivate;

import java.io.Serializable;
import java.util.List;

/**
 * 采购收货订单明细
 * Created by bingshanguxue on 15/9/22.
 */
public class InvSendIoOrderItemBrief implements ILongId, Serializable {
    private Long id;//订单编号
    private String barcode;//单据条码
    private String orderName;//单据编号
    private Double goodsFee;//商品金额
//    private Double transFee;//配送费
    private Double totalFee;//总金额
    @Deprecated
    private Double totalCount;//总数
    private Double commitGoodsNum;//
    private Integer status;//订单状态
    private Integer isPrivate = IsPrivate.PLATFORM;
    private Long sendTenantId;
    private String sendCompanyName;

    private List<InvSendIoOrderItem> items;

    private String captionStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

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

    public Double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Double totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Double totalCount) {
        this.totalCount = totalCount;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Double getCommitGoodsNum() {
        return commitGoodsNum;
    }

    public void setCommitGoodsNum(Double commitGoodsNum) {
        this.commitGoodsNum = commitGoodsNum;
    }

    public List<InvSendIoOrderItem> getItems() {
        return items;
    }

    public void setItems(List<InvSendIoOrderItem> items) {
        this.items = items;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Long getSendTenantId() {
        return sendTenantId;
    }

    public void setSendTenantId(Long sendTenantId) {
        this.sendTenantId = sendTenantId;
    }

    public String getSendCompanyName() {
        return sendCompanyName;
    }

    public void setSendCompanyName(String sendCompanyName) {
        this.sendCompanyName = sendCompanyName;
    }

    public String getCaptionStatus() {
        return captionStatus;
    }

    public void setCaptionStatus(String captionStatus) {
        this.captionStatus = captionStatus;
    }
}
