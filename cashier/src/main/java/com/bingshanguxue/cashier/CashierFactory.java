package com.bingshanguxue.cashier;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;

/**
 * Created by bingshanguxue on 7/7/16.
 */
public class CashierFactory {

    /**
     * 计算拆分子订单实际分配的支付金额,最多两位小数
     * 公式＝拆分订单金额/流水订单金额 * 实际支付金额
     */
    public static Double allocationPayableAmount(Double numerator, Double denominator, Double factor) {
        if (denominator == null || denominator.compareTo(0D) == 0
                || numerator == null || numerator.compareTo(0D) == 0
                || factor == null || factor.compareTo(0D) == 0) {
//            ZLogger.df(String.format("(%.2f / %.2f) * %.2f = %.2f",
//                    numerator, denominator, factor, 0D));
            return 0D;
        }

        Double result = (numerator / denominator) * factor;
        ZLogger.df(String.format("(%.2f / %.2f) * %.2f = %.2f",
                numerator, denominator, factor, result));
        return Double.valueOf(String.format("%.2f", result));
    }


    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * 终端号_订单编号_时间戳(13位)
     * @param orderId 订单编号
     * @param timeStampEnabled 是否添加时间戳在后面
     *
     * */
    public static String genTradeNo(Long orderId, boolean timeStampEnabled){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%d", SharedPreferencesManager.getTerminalId(),
                orderId));
        if (timeStampEnabled){
            sb.append("_").append(System.currentTimeMillis());
        }

        return sb.toString();
    }

    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * 格式：终端号_业务类型_支付类型_订单编号_时间戳(13位)
     * @param orderId 订单编号
     * @param timeStampEnabled 是否添加时间戳在后面
     * */
    public static String genTradeNo(Integer bizType, Integer payType, Long orderId, boolean timeStampEnabled){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%d_%d_%d", SharedPreferencesManager.getTerminalId(),
                bizType, payType, orderId));
        if (timeStampEnabled){
            sb.append("_").append(System.currentTimeMillis());
        }

        return sb.toString();
    }



}
