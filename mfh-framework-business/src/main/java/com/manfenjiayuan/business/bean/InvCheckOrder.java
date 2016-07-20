package com.manfenjiayuan.business.bean;

import java.util.Date;

/**
 * 库存盘点订单
 * Created by bingshanguxue on 2015/5/14.
 *
 */
public class InvCheckOrder implements java.io.Serializable{
    public static final Integer INVCHECK_ORDERSTATUS_PROCESSING = 0;//正在盘点（生成）
    public static final Integer INVCHECK_ORDERSTATUS_FINISHED = 2;//已确认（结束）
    public static final Integer INVCHECK_ORDERSTATUS_CANCELED = 9;//已取消
//    public static final Integer INVCHECK_ORDERSTATUS_FREEZE = 9;//已冻结

    private Long id;//盘点单号
    private String orderName;//单据名称
    private String netId;//当前操作所属门店或仓库
    private String tenantId;//所属租户
    private String createdBy;//创建人id
    private Integer status;//报损状态
    private Date createdDate;
    private Date updatedDate;
    private Double commitNum;//此处操作提交的sku数
    private Double commitGoodsNum;//提交的商品总数
    private Double commitPrice;//此处操作提交的商品总额

    private Double profitNum; //盘盈商品sku数
    private Double lossNum; //盘亏商品sku数
    private Double sameNum; //盘平商品sku数

    private Double invNum;//库存中的sku数
    private Double invGoodsNum;//库存的商品总数
    private Double invPrice = 0.0;//库存的商品总额

    private String statusCaption;
    private String storeTypeCaption;//
    private String netCaption;//网点名称


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

    public Double getProfitNum() {
        if (profitNum == null){
            return 0D;
        }
        return profitNum;
    }

    public void setProfitNum(Double profitNum) {
        this.profitNum = profitNum;
    }

    public Double getLossNum() {
        if (lossNum == null){
            return 0D;
        }
        return lossNum;
    }

    public void setLossNum(Double lossNum) {
        this.lossNum = lossNum;
    }

    public Double getSameNum() {
        if (sameNum == null){
            return 0D;
        }
        return sameNum;
    }

    public void setSameNum(Double sameNum) {
        this.sameNum = sameNum;
    }

    public String getStatusCaption() {
        return statusCaption;
    }

    public void setStatusCaption(String statusCaption) {
        this.statusCaption = statusCaption;
    }

    public String getStoreTypeCaption() {
        return storeTypeCaption;
    }

    public void setStoreTypeCaption(String storeTypeCaption) {
        this.storeTypeCaption = storeTypeCaption;
    }

    public String getNetCaption() {
        return netCaption;
    }

    public void setNetCaption(String netCaption) {
        this.netCaption = netCaption;
    }
}
