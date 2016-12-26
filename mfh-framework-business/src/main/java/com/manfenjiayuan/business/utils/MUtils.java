package com.manfenjiayuan.business.utils;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by bingshanguxue on 15/9/7.
 */
public class MUtils {
    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    public static String genOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + Math.abs(r.nextInt());
        key = key.substring(0, 15);
        return key;
    }

    /**
     * 订单条码号，12位毫秒数(自2015年1月1日以来的毫秒数,12位可存储大概30年范围）,基本确保全局唯一了。
     * */
    public static String getOrderBarCode() {
        Long timeStamp = System.currentTimeMillis();
//        ZLogger.d("timeStamp1: " + String.valueOf(TimeUtil.genTimeStamp()));//10位
//        ZLogger.d("timeStamp2: " + String.valueOf(System.currentTimeMillis()));//13 位

//        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
//                Locale.getDefault());
//        Date date = new Date();
//        String key = format.format(date);

        String barCode = String.valueOf(timeStamp);
        Random r = new Random();
        barCode = barCode + Math.abs(r.nextInt());
        barCode = barCode.substring(0, 12);
//        ZLogger.d("orderBarCode=" + barCode);
        return barCode;
    }

    /**
     * 生成订单条码：机器设备号＋业务类型＋时间戳
     * @param bizType 业务类型
     * */
    public static String genNewBarcode(int bizType) {
        return String.format("%d_%s_%d_%s", MfhLoginService.get().getCurOfficeId(),
                SharedPrefesManagerFactory.getTerminalId(), bizType, TimeUtil.genTimeStamp());
    }

    /**
     * 生成订单条码：网点编号＋机器设备号＋业务类型＋时间戳(确保唯一性)
     * 注意：机器设备号可能会变化，所以这里不再使用机器设备号。
     * @param bizType 业务类型
     * */
    public static String genDateBarcode(int bizType, Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
//        return String.format("%d_%s_%d_%s", MfhLoginService.get().getCurOfficeId(),
//                SharedPrefesManagerFactory.getTerminalId(), bizType,
//                simpleDateFormat.format(date));
        return String.format("%d_%d_%s", MfhLoginService.get().getCurOfficeId(), bizType,
                simpleDateFormat.format(date));
    }

    public static String formatDoubleWithSuffix(Double amount,
                                                String separator, String suffix){
        if (amount == null){
            return "无";
        }

        if (StringUtils.isEmpty(suffix)){
            return String.format("%.2f", amount);
        }
        else{
            return String.format("%.2f%s%s", amount, separator, suffix);
        }
    }

    /**
     * 格式化Double数据，格式：[prefix][preSeparator][amount/amountNullDesc][sufSeparator][suffix]
     * @param prefix
     * @param preSeparator
     * @param amount
     * @param amountNullDesc
     * @param sufSeparator
     * @param suffix
     * */
    public static String formatDouble(String prefix, String preSeparator,
                                                Double amount, String amountNullDesc,
                                                String sufSeparator, String suffix){
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(prefix)){
            sb.append(prefix);
            sb.append(preSeparator);
        }

        if (amount != null){
            sb.append(String.format("%.2f", amount));
        }
        else{
            sb.append(amountNullDesc);
        }

        if (!StringUtils.isEmpty(suffix)){
            sb.append(sufSeparator);
            sb.append(suffix);
        }

        return sb.toString();
    }

    public static String formatDouble(Double amount, String amountNullDesc){
        StringBuilder sb = new StringBuilder();
        if (amount != null){
            sb.append(String.format("%.2f", amount));
        }
        else{
            sb.append(amountNullDesc);
        }

        return sb.toString();
    }


    /**
     * 计算毛利率
     * 公式：毛利率＝毛利／营业额
     * */
    public static Double retrieveGrossMargin(Double turnover, Double grossProfit){
        if (grossProfit == 0D) {
            return 0D;
        }

        if (turnover == 0D) {
            return Double.valueOf(String.valueOf(Integer.MAX_VALUE));
        } else {
            return grossProfit / turnover;
        }
    }
    /**
     * 计算毛利率:格式11%
     * */
    public static String retrieveFormatedGrossMargin(Double turnover, Double grossProfit){
        Double grossMargin = retrieveGrossMargin(turnover, grossProfit);

        return String.format("%.2f%%", 100 * grossMargin);
    }

    /**
     * 快捷支付码
     * */
    public static String genQuickpamentCode(String raw, int length){
        if (StringUtils.isEmpty(raw)){
            return StringUtils.contact(length, '0');
        }
        else{
            int blackLen = Math.max(length - raw.length(), 0);
            if (blackLen > 0){
                return StringUtils.contact(blackLen, '0') + raw;
            }
            else{
                return raw;
            }
        }
    }

    /**
     * 解析卡芯片号，十六进制转换为十进制
     * 十六进制：466CAF31 (8位)
     * 十进制：1181527857 (10位)
     */
    public static String parseCardId(String rawData) {
        if (StringUtils.isEmpty(rawData)) {
            return null;
        }

        if (rawData.length() != 8){
            return null;
        }

        try {
            return String.valueOf(Long.parseLong(rawData, 16));
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    /**
     * 解析满分快捷支付码
     * 格式：000000000123456
     */
    public static String parseMfPaycode(String paycode) {
        if (StringUtils.isEmpty(paycode)) {
            return null;
        }

        //长度15位
        if (paycode.length() != 15){
            return null;
        }
            //这样判断不严谨，会错误的把其他0处理掉
//        int index = paycode.lastIndexOf("0");
//        String humanId2 = humanId.substring(index + 1, humanId.length());
        String humanId = paycode;
        while (humanId.startsWith("0")) {
            humanId = humanId.substring(1, humanId.length());
        }
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()){
            ZLogger.df(String.format("验证会员付款码: <%s> --> <%s>",
                    paycode, humanId));
        }

        return humanId;
    }

}
