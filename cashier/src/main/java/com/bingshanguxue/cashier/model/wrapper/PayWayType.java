package com.bingshanguxue.cashier.model.wrapper;

import java.util.HashMap;

/**
 * 支付记录类型
 * Created by bingshanguxue on 16/3/2.
 */
public class PayWayType {
    public final static Integer TYPE_NA              = 0;//未定义
    public final static Integer TYPE_CASH            = 1;//现金
    public final static Integer TYPE_CASH_CHANGE     = 2;//现金找零
    public final static Integer TYPE_ALIPAY_F2F      = 3;//支付宝——条码支付
    public final static Integer TYPE_ALIPAY_APP      = 4;//支付宝——APP支付
    public final static Integer TYPE_WEPAY_F2F       = 5;//微信--扫码付
    public final static Integer TYPE_WEPAY_APP       = 6;//微信——APP支付
    public final static Integer TYPE_BANKCARD        = 7;//银联
    public final static Integer TYPE_VIP             = 8;//会员账户支付：会员卡，付款码，手机号
    public final static Integer TYPE_VIP_DISCOUNT    = 9;//会员优惠
    public final static Integer TYPE_VIP_PROMOTION   = 10;//促销规则
    public final static Integer TYPE_VIP_COUPONS     = 11;//优惠券
    public final static Integer TYPE_VIP_BALANCE     = 12;//账户余额
    public final static Integer TYPE_VIP_SCORE       = 13;//积分支付
    public final static Integer TYPE_CREDIT          = 14;
    public final static Integer TYPE_THIRD_PARTY     = 15;//第三方外卖（支持第三单拣货，统一使用支付方式）


    private static HashMap<Integer, String> typeMaps = new HashMap<>();

    static {
        typeMaps.put(TYPE_CASH, "现金");
        typeMaps.put(TYPE_CASH_CHANGE, "找零");
        typeMaps.put(TYPE_ALIPAY_F2F, "支付宝条码支付");
        typeMaps.put(TYPE_WEPAY_F2F, "微信扫码付");
        typeMaps.put(TYPE_BANKCARD, "银联");
        typeMaps.put(TYPE_VIP, "会员账号支付");
        typeMaps.put(TYPE_VIP_DISCOUNT, "会员优惠");
        typeMaps.put(TYPE_VIP_PROMOTION, "促销优惠");
        typeMaps.put(TYPE_VIP_COUPONS, "优惠券");
        typeMaps.put(TYPE_VIP_BALANCE, "账户余额");
    }

    /**获取支付方式名称*/
    public static String getWayTypeName(Integer value){
        if (value != null){
            return typeMaps.get(value);
        }
        return null;
    }
}
