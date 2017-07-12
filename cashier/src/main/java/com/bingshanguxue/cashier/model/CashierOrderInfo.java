package com.bingshanguxue.cashier.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.WayType;

import java.util.List;

/**
 * 收银订单
 * Created by bingshanguxue on 2015/5/14.
 */
public class CashierOrderInfo implements java.io.Serializable {


    //==============订单信息开始======================
    //业务类型
    private Integer bizType;
    //订单条码
    private String posTradeNo;
    //商品总数量
    private Double bCount = 0D;
    //零售价总金额(＝零售价＊商品数量)
    private Double retailAmount = 0D;
    //成交价总金额(＝成交价＊商品数量)
    private Double finalAmount = 0D;
    //价格调整（＝零售价总金额-成交价总金额，正数表示优惠，负数表示价格调高）
    private Double adjustAmount = 0D;
    //折扣率（＝成交价总金额/零售价总金额）
    private Double discountRate = 0D;
    //==============订单信息结束======================


    //==============结算信息开始======================
    //订单编号
    private Long orderId;
    //主题
    private String subject;
    //商品简介，商品名称集合，逗号分隔。
    private String body = "";
    //商品明细, format like: [{skuId:..,bcount:...,price:...,whereId:...}]
    private JSONArray productsInfo;
    //==============结算信息结束======================


    //==============支付信息开始======================
    //已支付金额
    private Double paidAmount = 0D;
    //支付类型
    private Integer payType = WayType.NA;
    //会员
    private Human vipMember;
    //==============支付信息结束======================

    //卡券&促销规则（根据订单明细由后台计算）
    private MarketRulesWrapper mOrderMarketRules;
    //后台返回的订单支付相关信息
    private PayAmount mPayAmount;

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
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

    public Double getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(Double adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public MarketRulesWrapper getOrderMarketRules() {
        return mOrderMarketRules;
    }

    public void setOrderMarketRules(MarketRulesWrapper orderMarketRules) {
        mOrderMarketRules = orderMarketRules;
    }



    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }


    public String getPosTradeNo() {
        return posTradeNo;
    }

    public void setPosTradeNo(String posTradeNo) {
        this.posTradeNo = posTradeNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public JSONArray getProductsInfo() {
        return productsInfo;
    }

    public void setProductsInfo(JSONArray productsInfo) {
        this.productsInfo = productsInfo;
    }


    public Double getPaidAmount() {
        if (paidAmount == null) {
            return 0D;
        }
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Human getVipMember() {
        return this.vipMember;
    }

    public void setVipMember(Human vipMember) {
        this.vipMember = vipMember;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public PayAmount getPayAmount() {
        return mPayAmount;
    }

    public void setPayAmount(PayAmount payAmount) {
        mPayAmount = payAmount;
    }

    /**
     * 保存卡券和促销规则
     */
    public void couponPrivilege(List<MarketRulesWrapper> marketRulesList) {
        MarketRulesWrapper orderMarketRules = null;
        //优惠券和明细不匹配
        if (marketRulesList != null && marketRulesList.size() > 0) {
            orderMarketRules = marketRulesList.get(0);
//            orderMarketRules.setSplitOrderId(orderId);
//            orderMarketRules.setFinalAmount(finalAmount);
        }
        setOrderMarketRules(orderMarketRules);
    }

    //支付完成
    public void paid(Integer payType, Double amount) {
        //ZLogger.d("paid.payType" + this.payType);
        this.paidAmount += amount;
    }


    @Override
    public String toString() {
        return String.format("cashierOrderInfo:\n%s", JSONObject.toJSONString(this));
    }
}
