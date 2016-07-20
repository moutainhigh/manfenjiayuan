package com.manfenjiayuan.business.bean;

import java.util.Date;

/**
 * 报损订单
 * Created by Administrator on 2015/5/14.
 *
 */
public class InvLossOrder implements java.io.Serializable{
    public static final Integer INVLOSS_ORDERSTATUS_PROCESSING = 0;//生成，正在报损
    public static final Integer INVLOSS_ORDERSTATUS_FINISHED = 2;//已完成
    public static final Integer INVLOSS_ORDERSTATUS_CANCELED = 9;//已取消

    private Long id;//盘点单号
    private String orderName;//盘点名称
    private String netId;//网点编号
    private String tenantId;//所属租户
    private String createdBy;//创建人id
    private Integer status;//报损状态
    private Date createdDate;
    private Date updatedDate;
    private Double invNum;//报损商品sku库存数量
    private Double invGoodsNum;//报损商品库存数量
    private Double invPrice;//报损商品库存价格（金额）
    private Double commitNum;//报损商品sku数量
    private Double commitGoodsNum;//报损商品数量
    private Double commitPrice;//报损价格（金额）


    private String statusCaption;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getInvNum() {
        if (invNum == null){
            return 0D;
        }
        return invNum;
    }

    public void setInvNum(Double invNum) {
        this.invNum = invNum;
    }

    public Double getInvGoodsNum() {
        if (invGoodsNum == null){
            return 0D;
        }
        return invGoodsNum;
    }

    public void setInvGoodsNum(Double invGoodsNum) {
        this.invGoodsNum = invGoodsNum;
    }

    public Double getInvPrice() {
        if (invPrice == null){
            return 0D;
        }
        return invPrice;
    }

    public void setInvPrice(Double invPrice) {
        this.invPrice = invPrice;
    }

    public String getStatusCaption() {
        return statusCaption;
    }

    public void setStatusCaption(String statusCaption) {
        this.statusCaption = statusCaption;
    }

    public Double getCommitNum() {
        if (commitNum == null){
            return 0D;
        }
        return commitNum;
    }

    public void setCommitNum(Double commitNum) {
        this.commitNum = commitNum;
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

    public Double getCommitPrice() {
        if (commitPrice == null){
            return 0D;
        }
        return commitPrice;
    }

    public void setCommitPrice(Double commitPrice) {
        this.commitPrice = commitPrice;
    }
}
