package com.bingshanguxue.pda.bizz;

/**
 * Created by bingshanguxue on 15/9/7.
 * <p>
 * ARC--ACTIVITY_REQUEST_CODE
 * </p>
 */
public class ARCode {
    public static final int ARC_ANDROID_SETTINGS = 0x101;//系统设置

    public static final int ARC_DISTRIBUTION_SIGN = 0x03;//商品签收
    public static final int ARC_DISTRIBUTION_INSPECT = 0x04;//商品验货
    public static final int ARC_NATIVE_LOGIN = 0x05;//登录
    public static final int ARC_SENDORDER_LIST = 0x06;//采购订单列表
    public static final int ARC_INVCOMPANY_LIST = 0x07;//批发商
    public static final int ARC_OFFICE_LIST = 0x08;//租户列表
    public static final int ARC_SENDIOORDER_INSPECT = 0x09;//导入发货单
    public static final int ARC_SHELVES_LIST = 0x10;//盘点区域
    public static final int ARC_COMPANY_LIST = 0x08;//批发商列表
    public static final int ARC_INV_COMPROVIDER_LIST = 0x09;//批发商供应商列表
    public static final int ARC_INVFINDORDER_INSPECT = 0x11;//拣货单
    public static final int ARC_INSPECT_PRODUCTSKU = 0x12;//平台商品档案
    public static final int ARC_CASHIER = 0x13;//收银
    public static final int ARC_MY = 0x14;//我的
    public static final int ARC_ACCEPT_PREPAREORDER = 0x15;//买手-抢单
    public static final int ARC_RIDER_INSTOCK_PAY = 0x16;//骑手-妥投—补差价


}
