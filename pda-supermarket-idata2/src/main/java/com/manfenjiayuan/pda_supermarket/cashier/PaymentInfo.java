package com.manfenjiayuan.pda_supermarket.cashier;

import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderPayEntity;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.DiscountInfo;
import com.mfh.framework.api.constant.WayType;

import java.io.Serializable;

/**
 * 支付记录
 * Created by bingshanguxue on 7/1/16.
 */
public class PaymentInfo implements Serializable {
    private String outTradeNo;//交易编号,每次发起交易请求都不一样
    private int payType = WayType.NA;//支付方式
    private int status = PosOrderPayEntity.PAY_STATUS_INIT;//支付状态
    /**
     * 当前订单批次流水总的应付金额，用于计算拆分订单实际分配的支付金额。（包括优惠券优惠）
     * */
    private Double payableAmount = 0D;
    private Double paidAmount = 0D;//实际支付金额到账户的钱
    private Double change = 0D;//找零金额

    /**
     * 优惠信息（促销规则和优惠券优惠，如果优惠不为null,需要将优惠记录也另存为一条支付记录）
     * 多个拆分订单*/
    private DiscountInfo mDiscountInfo = null;

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public Double getPayableAmount() {
        if (payableAmount == null) {
            return 0D;
        }
        return payableAmount;
    }

    public void setPayableAmount(Double payableAmount) {
        this.payableAmount = payableAmount;
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

    public Double getChange() {
        if (change == null) {
            return 0D;
        }
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public DiscountInfo getDiscountInfo() {
        return mDiscountInfo;
    }

    public void setDiscountInfo(DiscountInfo discountInfo) {
        mDiscountInfo = discountInfo;
    }
}
