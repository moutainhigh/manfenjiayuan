package com.manfenjiayuan.pda_supermarket.cashier;

import com.mfh.framework.prefs.SharedPrefesManagerFactory;

/**
 * Created by bingshanguxue on 7/7/16.
 */
public class CashierFactory {

    /**
     * 生成商户订单号(64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。)
     * 终端号_订单编号_时间戳(13位)
     * @param orderId 订单编号
     * @param timeStampEnabled 是否添加时间戳在后面
     *
     * */
    public static String genTradeNo(Long orderId, boolean timeStampEnabled){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s_%d", SharedPrefesManagerFactory.getTerminalId(),
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
        sb.append(String.format("%s_%d_%d_%d", SharedPrefesManagerFactory.getTerminalId(),
                bizType, payType, orderId));
        if (timeStampEnabled){
            sb.append("_").append(System.currentTimeMillis());
        }

        return sb.toString();
    }



}
