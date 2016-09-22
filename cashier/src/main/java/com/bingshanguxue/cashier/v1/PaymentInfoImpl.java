package com.bingshanguxue.cashier.v1;

import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.cashier.model.wrapper.DiscountInfo;
import com.mfh.framework.api.account.Human;

/**
 * Created by bingshanguxue on 7/7/16.
 */
public class PaymentInfoImpl{

    /**
     * 订单支付记录
     * */
    public static PaymentInfo genPaymentInfo(String outTradeNo, int payType, int status,
                                             Double payableAmount, Double paidAmount, Double change){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPayType(payType);
        paymentInfo.setStatus(status);
        paymentInfo.setPayableAmount(payableAmount);
        paymentInfo.setPaidAmount(paidAmount);
        paymentInfo.setChange(change);
        return paymentInfo;
    }

    /**
     * 订单支付记录
     * */
    public static PaymentInfo genPaymentInfo(String outTradeNo, int payType, int status,
                                             Double payableAmount, Double paidAmount, Double change,
                                             DiscountInfo discountInfo){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPayType(payType);
        paymentInfo.setStatus(status);
        paymentInfo.setPayableAmount(payableAmount);
        paymentInfo.setPaidAmount(paidAmount);
        paymentInfo.setChange(change);
        paymentInfo.setDiscountInfo(discountInfo);
        return paymentInfo;
    }


    /**
     * 拆分并保存支付信息
     * */
    public static void saveOrUpdate(Long orderId, PaymentInfo paymentInfo, Human member){
        //商户交易订单号
        String outTradeNo = paymentInfo.getOutTradeNo();
        Double paidRemain = paymentInfo.getPaidAmount();//实际支付
        Double changeRemain = paymentInfo.getChange();//找零
        //支付状态
        int status = paymentInfo.getStatus();

        DiscountInfo discountInfo = paymentInfo.getDiscountInfo();
        if (discountInfo != null){
            //保存优惠金额
            PosOrderPayService.get().saveOrUpdate(outTradeNo, status, member, discountInfo);
        }

        //保存实际支付金额
        PosOrderPayService.get().saveOrUpdate(orderId,
                outTradeNo, paymentInfo.getPayType(),
                PosOrderPayEntity.AMOUNT_TYPE_IN, paidRemain,
                status, member);

        //保存找零金额
        if (changeRemain > 0.01) {
            PosOrderPayService.get().saveOrUpdate(orderId,
                    outTradeNo, paymentInfo.getPayType(),
                    PosOrderPayEntity.AMOUNT_TYPE_OUT, changeRemain,
                    status, member);
        }
    }
}
