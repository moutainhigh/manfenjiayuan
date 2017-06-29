package com.bingshanguxue.cashier;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.core.utils.MathCompact;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * 收银台
 * Created by bingshanguxue on 05/02/2017.
 */

public class CashierBenchObservable extends Observable {


    /** ============================ Bench ============================ */
    /**
     * 当前收银台收银商品,用列表的形式显示
     */
    private List<CashierShopcartEntity> shopcartEntities = new ArrayList<>();
    /**
     * 商品总数量
     */
    private Double bcount = 0D;
    /**
     * 商品总金额:在商品原价基础上调整后的成交价总金额
     */
    private Double finalAmount = 0D;
    /**
     * 商品总金额:在商品原价基础上调整后的成交价总金额（会员）
     */
    private Double finalCustomerAmount = 0D;
    /** ============================ Bench ============================ */



    /** ============================ Cashier ============================ */
    /**
     * 收银信息
     */
    private CashierOrderInfo cashierOrderInfo = null;

    /**会员优惠金额*/
    private Double itemRuleAmount = 0D;
    /**促销优惠金额*/
    private Double packRuleAmount = 0D;
    /**优惠券金额*/
    private Double couponAmount = 0D;
    /**赠送积分*/
    /** ============================ Cashier ============================ */

//    /**积分*/
//    private Double amount = 0D;
//    /**实付金额*/
//    private Double amount = 0D;
//    /**会员*/
//    private Human vipMember;

    private static CashierBenchObservable instance = null;

    /**
     * 返回 CashierDesktopObservable 实例
     *
     * @return
     */
    public static CashierBenchObservable getInstance() {
        if (instance == null) {
            synchronized (CashierBenchObservable.class) {
                if (instance == null) {
                    instance = new CashierBenchObservable();
                }
            }
        }
        return instance;
    }

    public List<CashierShopcartEntity> getShopcartEntities() {
        return shopcartEntities;
    }

    public void setShopcartEntities(List<CashierShopcartEntity> shopcartEntities) {
        this.shopcartEntities = shopcartEntities;

        onDatasetChanged();
    }

    public Double getBcount() {
        if (bcount == null) {
            return 0D;
        }
        return bcount;
    }

    public Double getFinalAmount() {
        if (finalAmount == null) {
            return 0D;
        }
        return finalAmount;
    }

    public Double getFinalCustomerAmount() {
        if (finalCustomerAmount == null) {
            return 0D;
        }
        return finalCustomerAmount;
    }

    public Double getItemRuleAmount() {
        if (itemRuleAmount == null) {
            return 0D;
        }
        return itemRuleAmount;
    }

    public Double getPackRuleAmount() {
        if (packRuleAmount == null) {
            return 0D;
        }
        return packRuleAmount;
    }

    public Double getCouponAmount() {
        if (couponAmount == null) {
            return 0D;
        }
        return couponAmount;
    }


    public CashierOrderInfo getCashierOrderInfo() {
        return cashierOrderInfo;
    }

    public void setCashierOrderInfo(CashierOrderInfo cashierOrderInfo) {
        this.cashierOrderInfo = cashierOrderInfo;

        PayAmount payAmount = cashierOrderInfo != null ? cashierOrderInfo.getPayAmount() : null;
        if (payAmount != null) {
            itemRuleAmount = payAmount.getItemRuleAmount();
            packRuleAmount = payAmount.getPackRuleAmount();
            couponAmount = payAmount.getCoupAmount();
        } else {
            itemRuleAmount = 0D;
            packRuleAmount = 0D;
            couponAmount = 0D;
        }

        notifyDatasetChanged();
    }

    /**
     * 清空数据
     */
    public void clear() {
        this.shopcartEntities = new ArrayList<>();
        this.cashierOrderInfo = null;
        onDatasetChanged();
    }

    /**
     * 数据发生改变
     */
    public void onDatasetChanged() {
        Double bcountTemp = 0D, amountTemp = 0D, customerAmountTemp = 0D;
        if (shopcartEntities != null && shopcartEntities.size() > 0) {
            for (CashierShopcartEntity entity : shopcartEntities) {
                bcountTemp += entity.getBcount();
                amountTemp += entity.getFinalAmount();
                customerAmountTemp += MathCompact.mult(entity.getBcount(), entity.getFinalCustomerPrice());
            }
        }
        this.bcount = bcountTemp;
        this.finalAmount = amountTemp;
        this.finalCustomerAmount = customerAmountTemp;

        notifyDatasetChanged();
    }

    private void notifyDatasetChanged() {
        setChanged();    //标记此 Observable对象为已改变的对象
        notifyObservers();    //通知所有观察者
    }



}
