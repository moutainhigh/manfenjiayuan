package com.bingshanguxue.cashier.v2;

import com.alibaba.fastjson.JSONArray;
import com.bingshanguxue.cashier.model.OrderMarketRules;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 收银订单拆分自订单支付信息
 * Created by bingshanguxue on 5/26/16.
 */
public class CashierOrderItemInfo implements Serializable {
    //关联订单数据库编号
    private Long orderId;
    //零售价总金额(＝零售价＊商品数量)
    private Double retailAmount = 0D;
    //成交价总金额(＝成交价＊商品数量)
    private Double finalAmount = 0D;
    //折扣率
    private Double discountRate = 0D;

    //折扣价(价格调整)（＝零售价总金额-成交价总金额，正数表示优惠，负数表示价格调高）
    private Double adjustDiscountAmount = 0D;

    private String couponsIds = "";//使用的卡券

    //卡券&促销规则
    private OrderMarketRules mOrderMarketRules;
    //促销or卡券优惠信息
    private DiscountInfo mDiscountInfo;

    //商品数量
    private Double bCount = 0D;
    //商品明细, format like: [{skuId:..,bcount:...,price:...,whereId:...}]
    private JSONArray productsInfo;
    //商品简介，商品名称集合，逗号分隔。
    private String brief = "";


    //支付类型
    private Integer payType = WayType.NA;
    //已支付金额
    private Double paidAmount = 0D;
    //找零
    private Double change = 0D;


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getRetailAmount() {
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Double getAdjustDiscountAmount() {
        return adjustDiscountAmount;
    }

    public void setAdjustDiscountAmount(Double adjustDiscountAmount) {
        this.adjustDiscountAmount = adjustDiscountAmount;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public JSONArray getProductsInfo() {
        return productsInfo;
    }

    public void setProductsInfo(JSONArray productsInfo) {
        this.productsInfo = productsInfo;
    }

    public OrderMarketRules getOrderMarketRules() {
        return mOrderMarketRules;
    }

    public void setOrderMarketRules(OrderMarketRules orderMarketRules) {
        mOrderMarketRules = orderMarketRules;
    }


    public DiscountInfo getDiscountInfo() {
        return mDiscountInfo;
    }

    public void setDiscountInfo(DiscountInfo discountInfo) {
        this.mDiscountInfo = discountInfo;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }
}
