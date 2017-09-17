package com.mfh.framework.api.cashier;


import com.mfh.framework.api.MfhApi;

/**
 * 收银Api<br>
 * <p/>
 * 采购单和销售单是同一个单据，不同的环节有不同的称呼，我们内部统称采销单，采销单用于不同小伙伴不同网点之间的商品流转；
 * 收货单和发货单是同一个单据，不同的环节有不同的称呼，我们内部统称收发单。
 * 调拨单是另一个单据，用于同一小伙伴内部不同网点之间的商品流转。
 * 调拨单和收发单统称配送单。
 * 出库单和入库单是两个单据。
 * 它们之间的关系是：
 * 一个采销单会分多次执行，每次执行生成一个收发单；
 * 收发单从生成、审核、发货、出库、物流、签收、入库有个流程；在流转过程中至少生成一次出库单和入库单。
 * 调拨单从生成、审核、出库、物流、入库有个流程；在流转过程中也至少生成一次出库单和入库单。
 * Created by NAT.ZZN(bingshanguxue) on 2015/9/12.
 */
public class CashierApi {
    /**
     * 店家商品建档
     */
    public static String URL_SCMARTGOODSSKU_SAVETENANTSKU = MfhApi.URL_BASE_SERVER + "/scMartGoodsSku/saveTenantSku";

    /**
     * 获取网点周边的小区 /shopSubdistRelation/findSubdisCodesByNetId?netId=133004 ，网点就是当前登录人员的curOffice，返回的是统一的编写型数据
     */
    public static String URL_FIND_SUBDISCODES_BY_NETID = MfhApi.URL_BASE_SERVER + "/shopSubdistRelation/findSubdisCodesByNetId";


    /**
     * pos端核销卡券:
     * /mineCoupons/abandonCouponById?mineCps=19,20&orderId=1_110
     */
    public static String URL_MIMECOUPONS_ABANDONCOUPON_BYID = MfhApi.URL_BASE_SERVER + "/mineCoupons/abandonCouponById";

    /**
     * 检查衣袋编号是否重复
     */
    public static String URL_MFHORDER_CHECK_PACKAGECODE = MfhApi.URL_BASE_SERVER + "/mfhorder/isPackageCodeExist";

    /**
     * 检查衣袋编号是否重复
     */
    public static String URL_MFHORDER_SAVE_LAUNDRYORDER = MfhApi.URL_BASE_SERVER + "/mfhorder/saveLaundryOrder";


    /**
     * 查询限额情况,第一个是限额（-1代表没有设置或限额无穷大,0代表限额就是0），第二个是未缴现金
     * /scNetRealInfo/queryLimitInfo
     */
    public static String URL_QUERYLIMITINFO = MfhApi.URL_BASE_SERVER + "/scNetRealInfo/queryLimitInfo";


    /**
     * 查pos订单的现金流水：
     * /orderPayWay/list?payType=1&officeId=136076&orderby=CREATED_DATE&orderbydesc=true
     */
    public static String URL_ORDERPAYWAY_LIST = MfhApi.URL_BASE_SERVER + "/orderPayWay/list";


    public static void register() {
        URL_SCMARTGOODSSKU_SAVETENANTSKU = MfhApi.URL_BASE_SERVER + "/scMartGoodsSku/saveTenantSku";
        URL_FIND_SUBDISCODES_BY_NETID = MfhApi.URL_BASE_SERVER + "/shopSubdistRelation/findSubdisCodesByNetId";
        URL_MIMECOUPONS_ABANDONCOUPON_BYID = MfhApi.URL_BASE_SERVER + "/mineCoupons/abandonCouponById";
        URL_MFHORDER_CHECK_PACKAGECODE = MfhApi.URL_BASE_SERVER + "/mfhorder/isPackageCodeExist";
        URL_MFHORDER_SAVE_LAUNDRYORDER = MfhApi.URL_BASE_SERVER + "/mfhorder/saveLaundryOrder";
        URL_QUERYLIMITINFO = MfhApi.URL_BASE_SERVER + "/scNetRealInfo/queryLimitInfo";
        URL_ORDERPAYWAY_LIST = MfhApi.URL_BASE_SERVER + "/orderPayWay/list";

    }

}
