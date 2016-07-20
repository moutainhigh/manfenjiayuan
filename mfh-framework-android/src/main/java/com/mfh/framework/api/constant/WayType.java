package com.mfh.framework.api.constant;

/**
 * 支付方式(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class WayType {
    public final static Integer NA          = 0;
    public final static Integer CASH        = 1;
    public final static Integer ALI_F2F     = 2;
    public final static Integer BANKCARD    = 4;
//    public final static Integer COUPONS     = 8;
    public final static Integer VIP         = 16;//会员：会员卡，付款码，手机号
    public final static Integer ALI         = 32;
    public final static Integer CREDIT      = 128;
    public final static Integer WX_F2F      = 256;
    public final static Integer WEIXIN      = 512;
    public final static Integer RULES      = 1024;//卡券和促销规则支付（卡券优惠部分）
    public final static Integer SCORE      = 2048;//积分支付

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
            sb.append("会员");
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
        if ((value & WEIXIN) == WEIXIN) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("微信");
        }
        if ((value & RULES) == RULES) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("卡券促销");
        }

        return sb.toString();
    }
}
