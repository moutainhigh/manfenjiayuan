package com.mfh.framework.api.commonuseraccount;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 支付金额
 * 适用场景：会员支付获取优惠券
 *
 *  <li>{@link #rpDisAmountMap} rpDisAmountMap 是针对每个卡券或促销规则在本订单中各自产生的优惠金额，他们之和应该等于(ruleAmount+coupAmount)</li>
 * Created by bingshanguxue on 6/29/16.
 */
public class PayAmount implements Serializable{
    private Double payAmount = 0D; // 优惠后总支付金额
    private Integer payType;//支付类型
    private Double transFee = 0D; // 其中：物流费用
    //促销规则优惠金额（包括促销规则和会员规则）ruleAmount= itemRuleAmount+packRuleAmount
    private Double ruleAmount = 0D;
    private Double itemRuleAmount = 0D;//会员优惠
    private Double packRuleAmount = 0D;//其他促销优惠
    private Double coupAmount = 0D;//卡券优惠
    private List<PayItem> payItemList;//补充信息
    private Map<String, Double> rpDisAmountMap;

    //前台选择的参数
    private String ruleIds = "";//促销规则编号,多个用逗号隔开
    private String couponsIds = "";//使用的优惠券编号,多个用逗号隔开

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }


    public Double getPayAmount() {
        if (payAmount == null){
            return 0D;
        }
        return payAmount;
    }

    public void setPayAmount(Double payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Double getTransFee() {
        if (transFee == null){
            return 0D;
        }
        return transFee;
    }

    public void setTransFee(Double transFee) {
        this.transFee = transFee;
    }

    public Double getRuleAmount() {
        if (ruleAmount == null){
            return 0D;
        }
        return ruleAmount;
    }

    public void setRuleAmount(Double ruleAmount) {
        this.ruleAmount = ruleAmount;
    }

    public Double getCoupAmount() {
        if (coupAmount == null){
            return 0D;
        }
        return coupAmount;
    }

    public void setCoupAmount(Double coupAmount) {
        this.coupAmount = coupAmount;
    }

    public List<PayItem> getPayItemList() {
        return payItemList;
    }

    public void setPayItemList(List<PayItem> payItemList) {
        this.payItemList = payItemList;
    }


    public Map<String, Double> getRpDisAmountMap() {
        return rpDisAmountMap;
    }

    public void setRpDisAmountMap(Map<String, Double> rpDisAmountMap) {
        this.rpDisAmountMap = rpDisAmountMap;
    }

    public Double getItemRuleAmount() {
        return itemRuleAmount;
    }

    public void setItemRuleAmount(Double itemRuleAmount) {
        this.itemRuleAmount = itemRuleAmount;
    }

    public Double getPackRuleAmount() {
        return packRuleAmount;
    }

    public void setPackRuleAmount(Double packRuleAmount) {
        this.packRuleAmount = packRuleAmount;
    }
}
