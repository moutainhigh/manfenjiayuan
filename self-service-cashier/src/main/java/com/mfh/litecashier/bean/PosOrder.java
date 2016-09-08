package com.mfh.litecashier.bean;

import com.mfh.comn.bean.ILongId;
import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 交易订单
 * Created by bingshanguxue on 15/9/22.
 */
public class PosOrder implements ILongId, Serializable {
    private Long id;//订单编号
    private String barcode;//订单条码
    private String formatTime;//下单时间
    private Double amount;//金额
    private Integer payType = WayType.NA; //支付方式 0-现金 1-满分 2-支付宝 3-微信 4-银联
    private Integer paystatus;//1-－已支付；0-－未支付
    private List<PosOrderItem> items;

    private String officeName;
    //    private Integer btype;
//    private String btypename;
//    private String subdisName;//小区名
//    private Long receiveStock;//收货网点id
//    private String receiveStockName;//收件网点名称
    private String receiveName;//收件人
    private String receivePhone;//收件人手机号
    private String address;//配送地址
    //    private String remark;
//    private Integer status;//订单状态
    private Date createdDate;//下单时间
//    private Date updatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFormatTime() {
        return formatTime;
    }

    public void setFormatTime(String formatTime) {
        this.formatTime = formatTime;
    }

    public String getReceiveName() {
        if (receiveName == null) {
            return "";
        }
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getReceivePhone() {
        if (receivePhone == null) {
            return "";
        }
        return receivePhone;
    }

    public void setReceivePhone(String receivePhone) {
        this.receivePhone = receivePhone;
    }

    public Double getAmount() {
        if (amount == null) {
            return 0D;
        }
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<PosOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PosOrderItem> items) {
        this.items = items;
    }

    public String getBarcode() {
        if (barcode == null) {
            return "";
        }
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getPaystatus() {
        return paystatus;
    }

    public void setPaystatus(Integer paystatus) {
        this.paystatus = paystatus;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }
}
