package com.bingshanguxue.cashier.model.wrapper;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单支付信息
 * Created by bingshanguxue on 7/4/16.
 */
public class OrderPayInfo implements Serializable {
    private Integer payType = WayType.NA;
    private List<PayWay> payWays = new ArrayList<>();//具体支付明细
    private List<PayWay> uploadPayWays = new ArrayList<>();
    private Double paidAmount = 0D;//已支付金额(订单实际金额)
    private Double change = 0D;//找零金额
    private Double promotionDiscount = 0D;//促销优惠
    private Double vipDiscount = 0D;//会员优惠
    private String ruleIds = "";
    private Double couponDiscount = 0D;//优惠券
    private String couponsIds = "";

//    public OrderPayInfo(Integer payType, List<PayWay> payWays, Double paidAmount, Double change, Double ruleDiscount, String couponsIds, String ruleIds) {
//        this.payType = payType;
//        this.payWays = payWays;
//        this.paidAmount = paidAmount;
//        this.change = change;
//        this.ruleDiscount = ruleDiscount;
//        this.couponsIds = couponsIds;
//        this.ruleIds = ruleIds;
//    }

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
        if (change == null) {
            return 0D;
        }
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getPromotionDiscount() {
        if (promotionDiscount == null) {
            return 0D;
        }
        return promotionDiscount;
    }

    public void setPromotionDiscount(Double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public Double getVipDiscount() {
        if (vipDiscount == null) {
            return 0D;
        }
        return vipDiscount;
    }

    public void setVipDiscount(Double vipDiscount) {
        this.vipDiscount = vipDiscount;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public Double getCouponDiscount() {
        if (couponDiscount == null) {
            return 0D;
        }
        return couponDiscount;
    }

    public void setCouponDiscount(Double couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public String getCouponsIds() {
        return couponsIds;
    }

    public void setCouponsIds(String couponsIds) {
        this.couponsIds = couponsIds;
    }


    public List<PayWay> getPayWays() {
        return payWays;
    }

    public void setPayWays(List<PayWay> payWays) {
        this.payWays = payWays;
    }

    public List<PayWay> getUploadPayWays() {
        return uploadPayWays;
    }

    public void setUploadPayWays(List<PayWay> uploadPayWays) {
        this.uploadPayWays = uploadPayWays;
    }

    /**
     * 统计支付记录
     */
    public static OrderPayInfo deSerialize(Long orderId) {
        if (orderId == null) {
            return null;
        }

        List<PosOrderPayEntity> payEntities = PosOrderPayService.get()
                .queryAllBy(String.format("orderId = '%d' and paystatus = '%d'",
                        orderId, PosOrderPayEntity.PAY_STATUS_FINISH));
        return deSerialize(payEntities);
    }

    /**
     * 统计支付记录
     */
    public static OrderPayInfo deSerialize(List<PosOrderPayEntity> payEntities) {
        Integer payType = WayType.NA;
        Double paidAmount = 0D, change = 0D, ruleDiscount = 0D, promotionAmount = 0D, couponDiscount = 0D;
        List<PayWay> payWays = new ArrayList<>();
        List<PayWay> uploadPayWays = new ArrayList<>();
        StringBuilder couponsIds = new StringBuilder();
        StringBuilder ruleIds = new StringBuilder();

        for (PosOrderPayEntity payEntity : payEntities) {
            Double amount = payEntity.getAmount();
//            Integer payType = payEntity.getPayType();
            Integer amountType = payEntity.getAmountType();

            payType = payType | payEntity.getPayType();

            PayWay payWay = new PayWay();
            payWay.setAmountType(amountType);
            payWay.setAmount(amount);
            payWay.setPayType(payEntity.getPayType());
            payWays.add(payWay);

            if (PayWayType.TYPE_CASH.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            }
            if (PayWayType.TYPE_CASH_CHANGE.equals(amountType)) {
                change += amount;
            } else if (PayWayType.TYPE_ALIPAY_F2F.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_BANKCARD.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_VIP.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_VIP_COUPONS.equals(amountType)) {
                couponDiscount += payEntity.getAmount();
                if (!StringUtils.isEmpty(payEntity.getCouponsIds())) {
                    if (couponsIds.length() > 0) {
                        couponsIds.append(",");
                    }
                    couponsIds.append(payEntity.getCouponsIds());
                }
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_VIP_DISCOUNT.equals(amountType)) {
                ruleDiscount += payEntity.getAmount();
                if (!StringUtils.isEmpty(payEntity.getRuleIds())) {
                    if (ruleIds.length() > 0) {
                        ruleIds.append(",");
                    }
                    ruleIds.append(payEntity.getRuleIds());
                }
                //因为会员折扣那块不能算支付类型，不提交
//                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_VIP_PROMOTION.equals(amountType)) {
                promotionAmount += payEntity.getAmount();
//                if (!StringUtils.isEmpty(payEntity.getRuleIds())) {
//                    if (promotionRuleIds.length() > 0) {
//                        promotionRuleIds.append(",");
//                    }
//                    promotionRuleIds.append(payEntity.getRuleIds());
//                }
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_WEPAY_F2F.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            } else if (PayWayType.TYPE_THIRD_PARTY.equals(amountType)) {
                paidAmount += amount;
                uploadPayWays.add(payWay);
            }
        }

        //TODO 对payWays排序

        OrderPayInfo wrapper = new OrderPayInfo();
        wrapper.setPayType(payType);
        wrapper.setPaidAmount(paidAmount);
        wrapper.setChange(change);
        wrapper.setVipDiscount(ruleDiscount);
        wrapper.setRuleIds(ruleIds.toString());
        wrapper.setPromotionDiscount(promotionAmount);
        wrapper.setCouponDiscount(couponDiscount);
        wrapper.setCouponsIds(couponsIds.toString());
        wrapper.setPayWays(payWays);
        wrapper.setUploadPayWays(uploadPayWays);
        return wrapper;

//        return new OrderPayInfo(payType, payWays, paidAmount, change,
//                ruleDiscount, couponsIds.toString(), ruleIds.toString());
    }


    public static Map<Integer, PayWay> getPayWays(OrderPayInfo orderPayInfo) {
        Map<Integer, PayWay> payWayMap = new HashMap<>();

        List<PayWay> payWays = orderPayInfo.getPayWays();
        if (payWays != null && payWays.size() > 0) {
            for (PayWay payWay : payWays) {
                Integer amountType = payWay.getAmountType();
                if (payWayMap.containsKey(amountType)) {
                    PayWay temp = payWayMap.get(amountType);
                    temp.setAmount(MathCompact.sub(temp.getAmount(), payWay.getAmount()));
                    payWayMap.put(amountType, temp);
                } else {
                    payWayMap.put(amountType, payWay);
                }
            }
        }

        return payWayMap;
    }

}
