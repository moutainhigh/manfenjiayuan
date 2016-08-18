package com.mfh.framework.api.invFindOrder;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;
import java.util.List;

/**
 * 拣货单明细
 * Created by bingshanguxue on 15/9/22.
 */
public class InvFindOrderItemBrief implements ILongId, Serializable {
    private Long id;//订单编号
    private String barcode;//单号
    private String orderName;//单据编号
    private Long targetNetId;//目标收货网点，根据拣货单发货会需要
    private String targetNetCaption;//收货网点名称

    private Double commitNum;//商品类别数量
    private Double commitGoodsNum;//商品总数量
    private Double totalFee;//总金额
    private Double totalCount;//总数
    private Integer status;//订单状态
    private String statusCaption;//订单状态


    private List<InvFindOrderItem> items;

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

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Double getCommitNum() {
        return commitNum;
    }

    public void setCommitNum(Double commitNum) {
        this.commitNum = commitNum;
    }

    public Double getCommitGoodsNum() {
        return commitGoodsNum;
    }

    public void setCommitGoodsNum(Double commitGoodsNum) {
        this.commitGoodsNum = commitGoodsNum;
    }

    public String getStatusCaption() {
        return statusCaption;
    }

    public void setStatusCaption(String statusCaption) {
        this.statusCaption = statusCaption;
    }

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

    public List<InvFindOrderItem> getItems() {
        return items;
    }

    public void setItems(List<InvFindOrderItem> items) {
        this.items = items;
    }


    public Long getTargetNetId() {
        return targetNetId;
    }

    public void setTargetNetId(Long targetNetId) {
        this.targetNetId = targetNetId;
    }

    public String getTargetNetCaption() {
        return targetNetCaption;
    }

    public void setTargetNetCaption(String targetNetCaption) {
        this.targetNetCaption = targetNetCaption;
    }
}
