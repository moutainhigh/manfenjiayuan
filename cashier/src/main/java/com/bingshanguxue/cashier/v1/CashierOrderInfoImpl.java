package com.bingshanguxue.cashier.v1;

import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;

import java.util.List;

/**
 * 收银信息
 * Created by bingshanguxue on 7/2/16.
 */
public class CashierOrderInfoImpl {
    /**
     * /**
     * 获取卡券优惠
     */
    public static Double getRuleDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
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
     */
    public static Double getCouponDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
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
     */
    public static Double getDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
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
     */
    public static Double getChange(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }

        CashierOrderItemInfo cashierOrderItemInfo = cashierOrderInfo.getCashierOrderItemInfo();
        if (cashierOrderItemInfo != null){
            return cashierOrderItemInfo.getChange();
        }
        else{
            return 0D;
        }
    }

    /**
     * 获取未支付的金额
     */
    public static Double getUnpayAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        CashierOrderItemInfo cashierOrderItemInfo = cashierOrderInfo.getCashierOrderItemInfo();
        if (cashierOrderItemInfo != null){
            return cashierOrderItemInfo.getFinalAmount() - cashierOrderItemInfo.getPaidAmount();
        }
        else{
            return 0D;
        }
    }

    /**
     * 计算当前应该支付金额, >0,表示应收金额; <0,表示找零金额
     * 公式：应收金额＝（商品总金额－价格调整）－已付金额 －促销优惠 －卡券优惠
     */
    public static Double getHandleAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        Double amount = 0D;
        CashierOrderItemInfo cashierOrderItemInfo = cashierOrderInfo.getCashierOrderItemInfo();
        if (cashierOrderItemInfo != null){
            Double temp = cashierOrderItemInfo.getFinalAmount() - cashierOrderItemInfo.getPaidAmount();
            DiscountInfo discountInfo = cashierOrderItemInfo.getDiscountInfo();
            if (discountInfo != null) {
                temp -= discountInfo.getEffectAmount();
            }

            //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
            //2016-07-04，判断需要放在循环里，因为折扣券是对拆分后的子订单生效，不是整个大订单
            //2016-08－15，现金支付完成后，重新计算应付金额，负数被忽略导致支付窗口没有关闭。
//            if (temp < 0.01){
//                temp = 0D;
//            }
            amount += temp;
        }

        //2016-08-15,实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
        if (amount < 0.01) {
            amount = 0D;
        }

        //精确到分
        return Double.valueOf(String.format("%.2f", amount));
    }
}
