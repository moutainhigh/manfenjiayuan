package com.manfenjiayuan.cashierdisplay.bean;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 收银订单
 * Created by Administrator on 2015/5/14.
 */
public class CashierOrderInfo implements java.io.Serializable {
    private final static int MAX_BODY_LENGTH = 20;

    //商品明细, format like: [{skuId:..,bcount:...,price:...,whereId:...}]
    private JSONArray productsInfo;
    //优惠券信息
    private JSONObject couponsInfo;
    //产品sku编号（查询优惠券）
    private JSONArray proSkuIds;
    //产品名称
    private String productNames;
    //数量
    private Double bCount = 0D;
    //总金额(零售价)，
    private Double retailAmount = 0D;
    //总金额(成交价)
    private Double dealAmount = 0D;
    //折扣价(会员优惠)
    private Double discountAmount = 0D;
    //折扣率
    private Double discountRate = 0D;
    //超市商家
    private Long companyId;
    //订单明细
    private List<PosOrderItemEntity> entityList;


    //以下字段支付的时候才会赋值
    //业务类型
    private Integer bizType;
    //订单条码
    private String orderBarcode;
    //订单编号
    private String orderId;
    private String subject;
    private String body;
    //实际支付金额（包含找零金额）
    private Double paidAmount = 0D;
    //规则or卡券
    private MarketRules marketRules;
    //规则
    private String ruleIds;
    //优惠券卡券领用号(已使用)
    private String couponsIds;
    //折扣价(卡券优惠)
    private Double couponDiscountAmount = 0D;
    //支付类型
    private Integer payType = WayType.NA;
    private Human vipMember;//会员

    //当前应该支付金额, >0,表示应收金额; <0,表示找零金额
    private Double handleAmount = 0D;


    public JSONArray getProductsInfo() {
        return productsInfo;
    }

    public void setProductsInfo(JSONArray productsInfo) {
        this.productsInfo = productsInfo;
    }

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    public JSONArray getProSkuIds() {
        return proSkuIds;
    }

    public void setProSkuIds(JSONArray proSkuId) {
        this.proSkuIds = proSkuId;
    }

    public Double getbCount() {
        return bCount;
    }

    public void setbCount(Double bCount) {
        this.bCount = bCount;
    }

    public Double getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(Double dealAmount) {
        this.dealAmount = dealAmount;
    }

    public Double getRetailAmount() {
        return retailAmount;
    }

    public void setRetailAmount(Double retailAmount) {
        this.retailAmount = retailAmount;
    }


    public JSONObject getCouponsInfo() {
        return couponsInfo;
    }

    public void setCouponsInfo(JSONObject couponsInfo) {
        this.couponsInfo = couponsInfo;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<PosOrderItemEntity> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<PosOrderItemEntity> entityList) {
        this.entityList = entityList;
    }

    public int getBizType() {
        return bizType;
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
    }

    public String getOrderBarcode() {
        return orderBarcode;
    }

    public void setOrderBarcode(String orderBarcode) {
        this.orderBarcode = orderBarcode;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
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

    public Double getHandleAmount() {
        return handleAmount;
    }

    public void setHandleAmount(Double handleAmount) {
        this.handleAmount = handleAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
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

    public void setPayType(int payType) {
        this.payType = payType;
    }


    public Human getVipMember() {
        return this.vipMember;
    }

    public void setVipMember(Human vipMember) {
        this.vipMember = vipMember;
    }

    public MarketRules getMarketRules() {
        return marketRules;
    }

    public void setMarketRules(MarketRules marketRules) {
        this.marketRules = marketRules;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public Double getCouponDiscountAmount() {
        return couponDiscountAmount;
    }

    public void setCouponDiscountAmount(Double couponDiscountAmount) {
        this.couponDiscountAmount = couponDiscountAmount;
    }

}
