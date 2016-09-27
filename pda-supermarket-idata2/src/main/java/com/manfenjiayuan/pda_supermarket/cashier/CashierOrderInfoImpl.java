package com.manfenjiayuan.pda_supermarket.cashier;


import com.manfenjiayuan.pda_supermarket.bean.wrapper.DiscountInfo;

/**
 * 收银信息
 * Created by bingshanguxue on 7/2/16.
 */
public class CashierOrderInfoImpl {
    /**
     * /**
     * 获取促销规则优惠
     */
    public static Double getRuleDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        DiscountInfo discountInfo = cashierOrderInfo.getDiscountInfo();
        return discountInfo != null ? discountInfo.getRuleDiscountAmount() : 0D;
    }

    /**
     * 获取卡券优惠
     */
    public static Double getCouponDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        DiscountInfo discountInfo = cashierOrderInfo.getDiscountInfo();
        return discountInfo != null ? discountInfo.getCouponDiscountAmount() : 0D;
    }

    /**
     * 获取所有优惠总金额
     */
    public static Double getDiscountAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        DiscountInfo discountInfo = cashierOrderInfo.getDiscountInfo();
        return discountInfo != null ? discountInfo.getEffectAmount() : 0D;
    }

    /**
     * 计算找零金额
     */
    public static Double getChange(CashierOrderInfo cashierOrderInfo) {
        return cashierOrderInfo != null ? cashierOrderInfo.getChange() : 0D;
    }

    /**
     * 获取未支付的金额
     */
    public static Double getUnpayAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        return cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount();
    }

    /**
     * 计算当前应该支付金额, >0,表示应收金额; <0,表示找零金额
     * 公式：应收金额＝（商品总金额－价格调整）－已付金额 －促销优惠 －卡券优惠
     */
    public static Double getHandleAmount(CashierOrderInfo cashierOrderInfo) {
        if (cashierOrderInfo == null) {
            return 0D;
        }
        Double amount = cashierOrderInfo.getFinalAmount() - cashierOrderInfo.getPaidAmount();
        DiscountInfo discountInfo = cashierOrderInfo.getDiscountInfo();
        if (discountInfo != null) {
            amount -= discountInfo.getEffectAmount();
        }

        //实际场景中应付金额不会小于1分钱，所以这里要保留两位小数
        //2016-07-04，判断需要放在循环里，因为折扣券是对拆分后的子订单生效，不是整个大订单
        //2016-08－15，现金支付完成后，重新计算应付金额，负数被忽略导致支付窗口没有关闭。
        if (amount < 0.01) {
            amount = 0D;
        }

        //精确到分
        return Double.valueOf(String.format("%.2f", amount));
    }
}
