package com.bingshanguxue.cashier.model.wrapper;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.CashierFactory;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.DailysettleService;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.vector_user.bean.Human;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                             List<DiscountInfo> discountInfos){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setPayType(payType);
        paymentInfo.setStatus(status);
        paymentInfo.setPayableAmount(payableAmount);
        paymentInfo.setPaidAmount(paidAmount);
        paymentInfo.setChange(change);
        paymentInfo.setDiscountInfos(discountInfos);
        return paymentInfo;
    }


    /**
     * 拆分支付信息
     * */
    public static void split(PaymentInfo paymentInfo, Integer bizType,
                             String orderBarCode, Human member){
        //参数检查
        List<PosOrderEntity> orderEntities = CashierFactory
                .fetchActiveOrderEntities(bizType, orderBarCode);
        if (orderEntities == null || orderEntities.size() <= 0) {
            ZLogger.df("拆分订单支付记录失败，订单不存在");
            return;
        }

        //商户交易订单号
        String outTradeNo = paymentInfo.getOutTradeNo();
        //订单实际分配金额=实际支付金额－找零金额
        Double denominatorAmount = paymentInfo.getPayableAmount();//分母
        Double paidFactor = paymentInfo.getPaidAmount();//实际支付
        Double paidRemain = paymentInfo.getPaidAmount();
        Double changeFactor = paymentInfo.getChange();//找零
        Double changeRemain = paymentInfo.getChange();
        //支付状态
        int status = paymentInfo.getStatus();

        Map<Long, DiscountInfo> discountInfoMap = new HashMap<>();
        List<DiscountInfo> discountInfos = paymentInfo.getDiscountInfos();
        if (discountInfos != null && discountInfos.size() > 0) {
            for (DiscountInfo discountInfo : discountInfos) {
                discountInfoMap.put(discountInfo.getOrderId(), discountInfo);
            }
        }

        //分配实际支付金额
        int startIndex = 0;
        int endIndex = orderEntities.size() - 1;
        for (PosOrderEntity orderEntity : orderEntities) {
            ZLogger.df(String.format("订单信息：\n%s", JSONObject.toJSONString(orderEntity)));
            OrderPayInfo payWrapper = OrderPayInfo.deSerialize(orderEntity.getId());
            ZLogger.df(String.format("订单支付信息：\n%s", JSONObject.toJSONString(payWrapper)));

            DiscountInfo discountInfo = discountInfoMap.get(orderEntity.getId());
            if (discountInfo == null){
                discountInfo = new DiscountInfo(orderEntity.getId());
            }
            Double paidAlloc, changeAlloc;
            if (startIndex == endIndex) {
                paidAlloc = paidRemain;
                changeAlloc = changeRemain;
            } else {
                Double numerator = orderEntity.getFinalAmount()
                        - payWrapper.getRuleDiscount() - discountInfo.getEffectAmount();

                paidAlloc = CashierFactory.allocationPayableAmount(numerator,
                        denominatorAmount, paidFactor);
                paidRemain -= paidAlloc;
                changeAlloc = CashierFactory.allocationPayableAmount(numerator,
                        denominatorAmount, changeFactor);
                changeRemain -= changeAlloc;
            }
            //保存实际支付金额
            PosOrderPayService.get().saveOrUpdate(orderEntity.getId(),
                    outTradeNo, paymentInfo.getPayType(),
                    PosOrderPayEntity.AMOUNT_TYPE_IN, paidAlloc,
                    status, member);

            //保存优惠金额
            PosOrderPayService.get().saveOrUpdate(outTradeNo, status, member, discountInfo);

            //保存找零金额
            if (changeAlloc > 0.01) {
                PosOrderPayService.get().saveOrUpdate(orderEntity.getId(),
                        outTradeNo, paymentInfo.getPayType(),
                        PosOrderPayEntity.AMOUNT_TYPE_OUT, changeAlloc,
                        status, member);
            }

            startIndex++;
        }
    }

    public static void saveOrUpdate(PaymentInfo paymentInfo, Integer bizType,
                             String orderBarCode, Human member){

        //日结支付记录
        if (BizType.DAILYSETTLE.equals(bizType)){
            List<DailysettleEntity> orderEntities  = DailysettleService.get()
                    .queryAllDesc(String.format("officeId = '%d' and barCode = '%s'",
                            MfhLoginService.get().getCurOfficeId(), orderBarCode), null);
            if (orderEntities == null || orderEntities.size() <= 0) {
                ZLogger.df("保存日结订单支付记录失败，订单不存在");
                return;
            }

            DailysettleEntity orderEntity = orderEntities.get(0);

            //商户交易订单号
            String outTradeNo = paymentInfo.getOutTradeNo();
            //订单实际分配金额=实际支付金额－找零金额
            Double denominatorAmount = paymentInfo.getPayableAmount();//分母
            Double paidFactor = paymentInfo.getPaidAmount();//实际支付
//            Double paidRemain = paymentInfo.getPaidAmount();
            Double changeFactor = paymentInfo.getChange();//找零
//            Double changeRemain = paymentInfo.getChange();
            //支付状态
            int status = paymentInfo.getStatus();

            //保存实际支付金额
            PosOrderPayService.get().saveOrUpdate(orderEntity.getId(),
                    outTradeNo, paymentInfo.getPayType(),
                    PosOrderPayEntity.AMOUNT_TYPE_IN, paidFactor,
                    status, member);
            return;
        }

        split(paymentInfo, bizType, orderBarCode, member);
    }
}
