package com.manfenjiayuan.pda_supermarket.cashier;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.CouponRule;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.DiscountInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.constant.WayType;

import java.util.List;
import java.util.Map;

/**
 * 收银订单
 * Created by Administrator on 2015/5/14.
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
    //找零金额
    private Double change = 0D;
    //会员
    private Human vipMember;
    //==============支付信息结束======================

    //卡券&促销规则（根据订单明细由后台计算）
    private MarketRulesWrapper mOrderMarketRules;
    //促销or卡券优惠信息
    private DiscountInfo mDiscountInfo;

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

    public DiscountInfo getDiscountInfo() {
        return mDiscountInfo;
    }

    public void setDiscountInfo(DiscountInfo discountInfo) {
        mDiscountInfo = discountInfo;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
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

    /**
     * 保存卡券和促销规则
     */
    public void couponPrivilege(List<MarketRulesWrapper> marketRulesList) {
        MarketRulesWrapper orderMarketRules = null;
        //优惠券和明细不匹配
        if (marketRulesList != null && marketRulesList.size() > 0) {
            orderMarketRules = marketRulesList.get(0);
        }
        setOrderMarketRules(orderMarketRules);
    }

    /**
     * 保存抵用优惠券,优惠券(代金券)优惠
     * @param amountArray 优惠金额
     * @param couponsMap
     */
    public boolean saveCouponDiscount(List<PayAmount> amountArray,
                                      String couponsIds, String rulesIds) {
        //检查输入参数是否正确
        if (amountArray == null ||
                amountArray.size() < 1) {
            ZLogger.d("输入参数不正确");
            return false;
        }
        ZLogger.d(String.format("保存会员/优惠券优惠金额\n%s", JSON.toJSONString(amountArray)));

        PayAmount payAmount = amountArray.get(0);

        //2016-07-04 这里的促销卡券优惠金额都是后台返回来的，可能单个值或累加值大于应付金额，
        // 保存的时候应该有所裁剪，否则可能会影响后面的计算结果
        DiscountInfo discountInfo = getDiscountInfo();

        discountInfo.setRuleDiscountAmount(payAmount.getRuleAmount());
        discountInfo.setCouponDiscountAmount(payAmount.getCoupAmount());
        discountInfo.setEffectAmount(finalAmount
                - paidAmount - payAmount.getPayAmount());
        // TODO: 5/26/16 选中的优惠券和订单可用的优惠券进行二次校验
        discountInfo.setCouponsIds(couponsIds);
        discountInfo.setRuleIds(rulesIds);
        setDiscountInfo(discountInfo);

        return true;
    }

    /**
     * VIP特权
     */
    public void vipPrivilege(Human vipMember) {
        this.vipMember = vipMember;
    }

    //支付完成
    public void paid(Integer payType, Double amount) {
        //ZLogger.d("paid.payType" + this.payType);
        this.paidAmount += amount;
    }


    @Override
    public String toString() {
        return String.format("cashierOrderInfo:\n%s", JSON.toJSONString(this));
    }
}
