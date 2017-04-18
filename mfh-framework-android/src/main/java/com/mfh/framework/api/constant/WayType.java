package com.mfh.framework.api.constant;

import java.util.HashMap;

/**
 * 支付方式(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class WayType {
    public final static Integer NA          = 0;
    public final static Integer CASH        = 1;
    public final static Integer ALI_F2F     = 2;//支付宝——条码支付
    public final static Integer BANKCARD    = 4;
//    public final static Integer COUPONS     = 8;
    public final static Integer VIP         = 16;//会员：会员卡，付款码，手机号
    public final static Integer ALI         = 32;//支付宝支付
    public final static Integer CREDIT      = 128;
    public final static Integer WX_F2F      = 256;//微信扫码付
    public final static Integer WEPAY_APP   = 512;//微信——APP支付
    public final static Integer RULES      = 1024;//卡券和促销规则支付（卡券优惠部分）
    public final static Integer SCORE      = 2048;//积分支付
    public final static Integer ALIPAY_APP  = 4096;//支付宝——APP支付
    public final static Integer TAKEOUT  = 16384;//第三方外卖（支持第三方外卖订单拣货，统一使用支付方式）


    private static HashMap<Integer, String> wayTypeMaps = new HashMap<>();

    static {
        wayTypeMaps.put(CASH, "现金");
        wayTypeMaps.put(ALI_F2F, "支付宝");
        wayTypeMaps.put(BANKCARD, "银联");
        wayTypeMaps.put(VIP, "会员账号支付");
        wayTypeMaps.put(ALI, "支付宝");
        wayTypeMaps.put(CREDIT, "赊账");
        wayTypeMaps.put(WX_F2F, "微信");
        wayTypeMaps.put(WEPAY_APP, "微信");
        wayTypeMaps.put(RULES, "会员优惠");//"卡券"改成"会员优惠"
        wayTypeMaps.put(SCORE, "积分");
        wayTypeMaps.put(ALIPAY_APP, "支付宝");
        wayTypeMaps.put(TAKEOUT, "第三方平台");
    }

    /**获取支付方式名称*/
    public static String getWayTypeName(Integer value){
        if (value != null){
            return wayTypeMaps.get(value);
        }
        return null;
    }

    public static String name(Integer value) {
        StringBuilder sb = new StringBuilder();
        if (value == null){
            return "";
        }
        if ((value & CASH) == CASH) {
            sb.append("现金");
        }
        if ((value & ALI_F2F) == ALI_F2F) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("支付宝");
        }
        if ((value & BANKCARD) == BANKCARD) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("银联");
        }
        if ((value & VIP) == VIP) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("会员账号支付");
        }
        if ((value & ALI) == ALI) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("支付宝");
        }
        if ((value & CREDIT) == CREDIT) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("赊账");
        }
        if ((value & WX_F2F) == WX_F2F) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("微信");
        }
        if ((value & WEPAY_APP) == WEPAY_APP) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("微信App支付");
        }
        if ((value & RULES) == RULES) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("会员优惠");
        }
        if ((value & ALIPAY_APP) == ALIPAY_APP) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("支付宝App支付");
        }
        if ((value & TAKEOUT) == TAKEOUT) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("第三方平台");
        }

        return sb.toString();
    }
}
