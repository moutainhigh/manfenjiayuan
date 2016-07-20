package com.bingshanguxue.cashier.model.wrapper;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 7/4/16.
 */
public class OrderPayInfo implements Serializable {
    private Integer payType = WayType.NA;
    private Double paidAmount = 0D;//已支付金额
//    private JSONArray payWays = new JSONArray();//具体支付明细
    private List<PayWay> payWays = new ArrayList<>();
    private Double change = 0D;//找零
    private Double ruleDiscount = 0D;//促销优惠
    private String couponsIds = "";
    private String ruleIds = "";

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

    public Double getRuleDiscount() {
        return ruleDiscount;
    }

    public void setRuleDiscount(Double ruleDiscount) {
        this.ruleDiscount = ruleDiscount;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public List<PayWay> getPayWays() {
        return payWays;
    }

    public void setPayWays(List<PayWay> payWays) {
        this.payWays = payWays;
    }

    /**
     * 统计支付记录
     */
    public static OrderPayInfo deSerialize(Long orderId) {
        if(orderId == null){
            return null;
        }

        List<PosOrderPayEntity> payEntities = PosOrderPayService.get()
                .queryAllBy(String.format("orderId = '%s' and paystatus = '%d'",
                        orderId, PosOrderPayEntity.PAY_STATUS_FINISH));
        return deSerialize(payEntities);
    }

    /**
     * 统计支付记录
     */
    public static OrderPayInfo deSerialize(List<PosOrderPayEntity> payEntities) {
        Integer payType = WayType.NA;
        Double paidAmount = 0D, change = 0D, ruleDiscount = 0D;
        List<PayWay> payWays = new ArrayList<>();
        StringBuilder couponsIds = new StringBuilder();
        StringBuilder ruleIds = new StringBuilder();

        for (PosOrderPayEntity payEntity : payEntities) {
            Double amount = payEntity.getAmount();

            payType = payType | payEntity.getPayType();
            if (PosOrderPayEntity.AMOUNT_TYPE_IN.equals(payEntity.getAmountType())) {
                paidAmount += amount;

                PayWay payWay = new PayWay();
                payWay.setPayType(payEntity.getPayType());
                payWay.setAmount(amount);
                payWays.add(payWay);

                if ((payEntity.getPayType() & WayType.RULES) == WayType.RULES) {
                    ruleDiscount += payEntity.getAmount();
                    if (!StringUtils.isEmpty(payEntity.getCouponsIds())) {
                        if (couponsIds.length() > 0) {
                            couponsIds.append(",");
                        }
                        couponsIds.append(payEntity.getCouponsIds());
                    }
                    if (!StringUtils.isEmpty(payEntity.getRuleIds())) {
                        if (ruleIds.length() > 0) {
                            ruleIds.append(",");
                        }
                        ruleIds.append(payEntity.getRuleIds());
                    }
                }
            } else {
                change += amount;
            }
        }

        OrderPayInfo wrapper = new OrderPayInfo();
        wrapper.setPayType(payType);
        wrapper.setPaidAmount(paidAmount);
        wrapper.setChange(change);
        wrapper.setRuleDiscount(ruleDiscount);
        wrapper.setCouponsIds(couponsIds.toString());
        wrapper.setRuleIds(ruleIds.toString());
        wrapper.setPayWays(payWays);

        return wrapper;
    }

}
