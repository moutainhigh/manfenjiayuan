package com.manfenjiayuan.cashierdisplay.bean;

/**
 * 支付方式(前后台统一)
 * Created by bingshanguxue on 16/3/2.
 */
public class WayType {
    public final static Integer NA           = 0;
    public final static Integer CASH         = 1;
    public final static Integer ALI_F2F      = 2;
    public final static Integer BANKCARD     = 4;
    public final static Integer COUPONS      = 8;
    public final static Integer MFACCOUNT    = 16;
    public final static Integer ALI          = 32;
    public final static Integer WEIX         = 64;
    public final static Integer CREDIT       = 128;
    public final static Integer WX_F2F       = 256;

    public static String name(Integer value) {
        StringBuilder sb = new StringBuilder();
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
        if ((value & COUPONS) == COUPONS) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("满分充值卡");
        }
        if ((value & MFACCOUNT) == MFACCOUNT) {
            if (sb.length() > 0){
                sb.append(",");
            }
            sb.append("满分账户");
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

        return sb.toString();
    }
}
