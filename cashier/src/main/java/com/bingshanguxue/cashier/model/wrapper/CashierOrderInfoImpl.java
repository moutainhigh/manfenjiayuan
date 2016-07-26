package com.bingshanguxue.cashier.model.wrapper;

import java.util.List;

/**
 * 收银信息
 * Created by bingshanguxue on 7/2/16.
 */
public class CashierOrderInfoImpl{
    /**
    /**
     * 获取卡券优惠
     * */
    public static Double getRuleDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<DiscountInfo> discountInfos = cashierOrderInfo.getDiscountInfos();

        Double amount = 0D;
        if (discountInfos != null && discountInfos.size() > 0) {
            for (DiscountInfo discountInfo : discountInfos) {
                amount += discountInfo.getRuleDiscountAmount();
            }
        }
        return amount;
    }

    /**
     * 获取卡券优惠
     * */
    public static Double getCouponDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<DiscountInfo> discountInfos = cashierOrderInfo.getDiscountInfos();
        Double amount = 0D;
        if (discountInfos != null && discountInfos.size() > 0) {
            for (DiscountInfo discountInfo : discountInfos) {
                amount += discountInfo.getCouponDiscountAmount();
            }
        }
        return amount;
    }
    /**
     * 获取所有优惠总金额
     * */
    public static Double getDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<DiscountInfo> discountInfos = cashierOrderInfo.getDiscountInfos();

        Double amount = 0D;
        if (discountInfos != null && discountInfos.size() > 0) {
            for (DiscountInfo discountInfo : discountInfos) {
                amount += discountInfo.getEffectAmount();
            }
        }
        return amount;
    }

    /**
     * 计算找零金额
     * */
    public static Double getChange(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<CashierOrderItemInfo> cashierOrderItemInfos = cashierOrderInfo.getCashierOrderItemInfos();

        Double amount = 0D;
        if (cashierOrderItemInfos != null && cashierOrderItemInfos.size() > 0) {
            for (CashierOrderItemInfo itemInfo : cashierOrderItemInfos) {
                amount += itemInfo.getChange();
            }
        }
        return amount;
    }

    /**
     * 获取未支付的金额
     */
    public static Double getUnpayAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<CashierOrderItemInfo> cashierOrderItemInfos = cashierOrderInfo.getCashierOrderItemInfos();

        Double amount = 0D;
        if (cashierOrderItemInfos != null && cashierOrderItemInfos.size() > 0) {
            for (CashierOrderItemInfo itemInfo : cashierOrderItemInfos) {
                amount += (itemInfo.getFinalAmount() - itemInfo.getPaidAmount());
            }
        }
        return amount;
    }

    /**
     * 计算当前应该支付金额, >0,表示应收金额; <0,表示找零金额
     * 公式：应收金额＝（商品总金额－价格调整）－已付金额 －促销优惠 －卡券优惠
     */
    public static Double getHandleAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null){
            return 0D;
        }
        List<CashierOrderItemInfo> cashierOrderItemInfos = cashierOrderInfo.getCashierOrderItemInfos();

        Double amount = 0D;
        for (CashierOrderItemInfo itemInfo : cashierOrderItemInfos) {
            Double temp = itemInfo.getFinalAmount() - itemInfo.getPaidAmount();
            DiscountInfo discountInfo = itemInfo.getDiscountInfo();
            if (discountInfo != null){
                temp -= discountInfo.getEffectAmount();
            }

            //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
            //2016-07-04，判断需要放在循环里，因为折扣券是对拆分后的子订单生效，不是整个大订单
            if (temp < 0.01){
                temp = 0D;
            }
            amount += temp;
        }

        //精确到分
        return Double.valueOf(String.format("%.2f", amount));
    }

    /**
     * 根据拆分订单编号找到订单支付信息
     */
    public static CashierOrderItemInfo getCashierOrderItemInfo(CashierOrderInfo cashierOrderInfo,
                                                               Long orderId) {
        if (cashierOrderInfo == null || orderId == null) {
            return null;
        }

        List<CashierOrderItemInfo> cashierOrderItemInfos = cashierOrderInfo.getCashierOrderItemInfos();
        if (cashierOrderItemInfos != null && cashierOrderItemInfos.size() > 0){
            for (CashierOrderItemInfo itemInfo : cashierOrderItemInfos) {
                if (itemInfo.getOrderId().equals(orderId)) {
                    return itemInfo;
                }
            }
        }

        return null;
    }
}
