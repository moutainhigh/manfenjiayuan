package com.manfenjiayuan.pda_supermarket;

/**
 * 常量
 * Created by NAT.ZZN(bingshanguxue) on 15/9/7.<br>
 * ARC (ACTIVITY_REQUEST_CODE)<br>
 * BA  (BROADCAST_ACTION)<br>
 * CK  (CACHE_KEY)<br>
 * TCK  (TEMP_CACHE_KEY)<br>
 */
public class Constants {
    /*应付金额发生变化*/
    public static final String BA_HANDLE_AMOUNT_CHANGED         = "BA_HANDLE_AMOUNT_CHANGED";//现金
    public static final String BA_HANDLE_AMOUNT_CHANGED_ALIPAY  = "BA_HANDLE_AMOUNT_CHANGED_ALIPAY";//支付宝
    public static final String BA_HANDLE_AMOUNT_CHANGED_WX      = "BA_HANDLE_AMOUNT_CHANGED_WX";//微信
    public static final String BA_HANDLE_AMOUNT_CHANGED_VIP     = "BA_HANDLE_AMOUNT_CHANGED_MFCARD";//会员
    public static final String BA_HANDLE_SCANBARCODE     = "BA_HANDLE_SCANBARCODE";//扫描条码

}
