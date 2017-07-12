package com.mfh.framework.rxapi.bean;

import com.mfh.framework.api.abs.MfhEntity;
import com.mfh.framework.api.constant.WayType;

import java.util.Date;
import java.util.List;

import me.drakeet.multitype.Item;

/**
 * 交易订单
 * Created by bingshanguxue on 15/9/22.
 */
public class GoodsOrder extends MfhEntity<Long> implements Item {
//    private Long id;//订单编号
//    private Long posOrderId;//pos机订单编号
    private String barcode;//订单条码
    private String formatTime;//下单时间
    private Double amount;//金额
    private Double bcount;//商品数量
    private Integer payType = WayType.NA; //支付方式 0-现金 1-满分 2-支付宝 3-微信 4-银联
    private Integer paystatus;//1-－已支付；0-－未支付
    private List<GoodsOrderItem> items;
    private String officeName;
    private Integer btype;//业务类型
    private Integer subType;//子业务类型

//    private String btypename;
//    private String subdisName;//小区名
//    private Long receiveStock;//收货网点id
//    private String receiveStockName;//收件网点名称
    //    private String remark;
//    private Integer status;//订单状态

    private String outerNo;//外部订单编号（外部平台订单组货功能特有）


    /**
     * 客户
     * */
    private String buyerName;//客户(也就是买家)昵称,客户不一定是收件人

    /**
     * 这部分是线上订单特有的
     * */
    private String address; //完整的收件地址(冗余)，也可能是驿站的地址（驿站地址的话要再加上电话号码）
    private String receiveName;// 最终收件人名称
    private String receivePhone;// 最终收件人手机
    private Double transFee;//订单总金额中的其中物流费用
    private Date dueDate;//送达时间-开始
    private Date dueDateEnd;//送达时间-结束
    private String remark ="";    //备注

    private Double disAmount;//券、促销规则等计算出的优惠金额,这部分金额由发券方承担，但应计入营业额



    /**
     * 小伙伴(买手)*/
    private Long guideHumanId;//买手人员编号
    private String serviceHumanName;//小伙伴(买手)名称
    private String serviceMobile;//小伙伴手机
    private String serviceHumanImg;//买手头像


    private Double commitAmount;//拣货金额

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

    public Double getBcount() {
        return bcount;
    }

    public void setBcount(Double bcount) {
        this.bcount = bcount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<GoodsOrderItem> getItems() {
        return items;
    }

    public void setItems(List<GoodsOrderItem> items) {
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

    public Integer getBtype() {
        return btype;
    }

    public void setBtype(Integer btype) {
        this.btype = btype;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getGuideHumanId() {
        return guideHumanId;
    }

    public void setGuideHumanId(Long guideHumanId) {
        this.guideHumanId = guideHumanId;
    }

    public String getServiceHumanName() {
        return serviceHumanName;
    }

    public void setServiceHumanName(String serviceHumanName) {
        this.serviceHumanName = serviceHumanName;
    }

    public String getServiceMobile() {
        return serviceMobile;
    }

    public void setServiceMobile(String serviceMobile) {
        this.serviceMobile = serviceMobile;
    }

    public String getServiceHumanImg() {
        return serviceHumanImg;
    }

    public void setServiceHumanImg(String serviceHumanImg) {
        this.serviceHumanImg = serviceHumanImg;
    }

    public Double getTransFee() {
        return transFee;
    }

    public void setTransFee(Double transFee) {
        this.transFee = transFee;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getDueDateEnd() {
        return dueDateEnd;
    }

    public void setDueDateEnd(Date dueDateEnd) {
        this.dueDateEnd = dueDateEnd;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Double getCommitAmount() {
        if (commitAmount == null) {
            return 0D;
        }
        return commitAmount;
    }

    public void setCommitAmount(Double commitAmount) {
        this.commitAmount = commitAmount;
    }

    public String getOuterNo() {
        return outerNo;
    }

    public void setOuterNo(String outerNo) {
        this.outerNo = outerNo;
    }

    public Double getDisAmount() {
        return disAmount;
    }

    public void setDisAmount(Double disAmount) {
        this.disAmount = disAmount;
    }
}
