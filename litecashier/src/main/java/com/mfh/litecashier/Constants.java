package com.mfh.litecashier;

/**
 * 常量
 * Created by NAT.ZZN(bingshanguxue) on 15/9/7.<br>
 * ARC (ACTIVITY_REQUEST_CODE)<br>
 * BA  (BROADCAST_ACTION)<br>
 * CK  (CACHE_KEY)<br>
 * TCK  (TEMP_CACHE_KEY)<br>
 */
public class Constants {
    public static final int ARC_MFPAY                           = 0x01;//支付
    public static final int ARC_NATIVE_LOGIN                    = 0x02;//登录
    public static final int ARC_CREATE_PURCHASE_GOODS           = 0x03;//新增采购商品
    public static final int ARC_GENERATE_PURCHASE_GOODS         = 0x04;//新增采购商品
    public static final int ARC_APPLY_SHOPCART                  = 0x05;//采购商品购物车
    public static final int ARC_CREATE_PURCHASE_ORDER           = 0x06;//新建采购单
    public static final int ARC_CREATE_PURCHASE_RECEIPT_ORDER   = 0x07;//新建采购收货单
    public static final int ARC_CREATE_PURCHASE_RETURN_ORDER    = 0x08;//新建采购退货单
    public static final int ARC_CREATE_STOCK_BATCH              = 0x09;//新建库存批次
    public static final int ARC_SELECT_WHOLESALER_TENANT        = 0x21;//选择批发商&门店
    public static final int ARC_HANDOVER                        = 0x30;//交接班
    public static final int ARC_DAILY_SETTLE                    = 0x31;//交接班
    public static final int ARC_SELECT_STOCKGOODS               = 0x32;//选择库存商品

    /*发送支付请求*/
    public static final String BA_SEND_PAY_REQ_CASH     = "BA_SEND_PAY_REQ_CASH";//--现金
    public static final String BA_SEND_PAY_REQ_ALIPAY   = "BA_SEND_PAY_REQ_ALIPAY";//－－支付宝
    public static final String BA_SEND_PAY_REQ_WX       = "BA_SEND_PAY_REQ_WX";//－－微信
    public static final String BA_SEND_PAY_REQ_BANK     = "BA_SEND_PAY_REQ_BANK";//－－银行卡
    public static final String BA_SEND_PAY_REQ_MFCARD   = "BA_SEND_PAY_REQ_MFCARD";//－－满分卡
    public static final String BA_SEND_PAY_REQ_MFACCOUNT    = "BA_SEND_PAY_REQ_MFACCOUNT";  //－－满分账户
    public static final String BA_SEND_PAY_REQ_CREDIT       = "BA_SEND_PAY_REQ_CREDIT";     //－－赊账
    /*应付金额发生变化*/
    public static final String BA_HANDLE_AMOUNT_CHANGED         = "BA_HANDLE_AMOUNT_CHANGED";//现金
    public static final String BA_HANDLE_AMOUNT_CHANGED_ALIPAY  = "BA_HANDLE_AMOUNT_CHANGED_ALIPAY";//支付宝
    public static final String BA_HANDLE_AMOUNT_CHANGED_WX      = "BA_HANDLE_AMOUNT_CHANGED_WX";//微信
    public static final String BA_HANDLE_AMOUNT_CHANGED_BANK    = "BA_HANDLE_AMOUNT_CHANGED_BANK";//银行卡
    public static final String BA_HANDLE_AMOUNT_CHANGED_MFCARD  = "BA_HANDLE_AMOUNT_CHANGED_MFCARD";//满分卡
    public static final String BA_HANDLE_AMOUNT_CHANGED_MFACCOUNT   = "BA_HANDLE_AMOUNT_CHANGED_MFACCOUNT";//满分账户
    public static final String BA_HANDLE_AMOUNT_CHANGED_CREDIT  = "BA_HANDLE_AMOUNT_CHANGED_CREDIT";//赊账
    //

    public static final String EXTRA_KEY_HANDLE_AMOUNT = "EXTRA_KEY_HANDLE_AMOUNT";//应付金额


    //    orderStatus:订单通用状态
//    -1:     初始状态
//    0:      已下单,订单生效
//    100:    已经取消
//    1:      待发货
//    2:      配送中
//    3:      已到达
//    4:      已签收
    public static final int ORDER_STATUS_INIT = -1;
    public static final int ORDER_STATUS_ORDERED = 0;
    public static final int ORDER_STATUS_CANCELED = 100;
    public static final int ORDER_STATUS_WAIT_SHIP = 1;
    public static final int ORDER_STATUS_DELIVER = 2;
    public static final int ORDER_STATUS_ARRIVED = 3;
    public static final int ORDER_STATUS_RECEIVED = 4;

}
