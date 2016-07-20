package com.manfenjiayuan.business.bean;

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
    private Long targetNetId;//目标收货网点，根据拣货单发货会需要
    private String targetNetCaption;//收货网点名称

    private Double goodsFee;//商品金额
//    private Double transFee;//配送费
    private Double totalFee;//总金额
    private Double totalCount;//总数
    private Integer status;//订单状态


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
