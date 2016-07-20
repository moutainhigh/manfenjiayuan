package com.mfh.framework.pay.umsips;

/**
 * 交易类型(transType)
 * String
 * 长度：2
 * 必填
 Apptype=’01’银行卡交易：
 '00'-消费   '01'-撤销  '02'-查余   '03'-退货   '04'-结算   '05'-签到
 Apptype=’02’预付卡交易：
 '00'-消费   '01'-撤销  '02'-查余   '03'-退货   '04'-结算   '05'-签到
 Apptype=’03’本地交易："

 * Created by bingshanguxue on 4/12/16.
 */
public class TransType {
    public static final String CONSUMER     = "00";//消费
    public static final String REVOCATION   = "01";//撤销
    public static final String CHECK        = "02";//查余
    public static final String RETURN       = "03";//退货
    public static final String SETTLEMENT   = "04";//结算
    public static final String SIGN         = "05";//签到
}
