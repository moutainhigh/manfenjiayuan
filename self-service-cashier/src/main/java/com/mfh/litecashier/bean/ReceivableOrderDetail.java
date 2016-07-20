package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;

import java.io.Serializable;
import java.util.List;

/**
 *  接收商品订单明细
 * Created by bingshanguxue on 15/9/22.
 */
public class ReceivableOrderDetail implements ILongId, Serializable {
    private Long id;//订单编号
    private String barcode;//单号
    private String sendDate;//发货时间
    private Double goodsFee;//商品金额
//    private Double transFee;//配送费
    private Double totalFee;//总金额
    private String transHumanName;//配送员
    private Integer status;//订单状态

    private List<ReceivableOrderItem> items;

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


    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public Double getGoodsFee() {
        return goodsFee;
    }

    public void setGoodsFee(Double goodsFee) {
        this.goodsFee = goodsFee;
    }
//
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

    public String getTransHumanName() {
        return transHumanName;
    }

    public void setTransHumanName(String transHumanName) {
        this.transHumanName = transHumanName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<ReceivableOrderItem> getItems() {
        return items;
    }

    public void setItems(List<ReceivableOrderItem> items) {
        this.items = items;
    }
}
