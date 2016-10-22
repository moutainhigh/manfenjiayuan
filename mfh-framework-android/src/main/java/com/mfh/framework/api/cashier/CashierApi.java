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
    public final static String URL_SCMARTGOODSSKU_SAVETENANTSKU = MfhApi.URL_BASE_SERVER + "/scMartGoodsSku/saveTenantSku";


    /**
     * pos端批量将未同步订单数据同步到云端，并修改库存
     */
    public static final String URL_POS_BATCH_INORDERS = MfhApi.URL_BASE_SERVER + "/posOrder/batchInOrders";


    /**
     * 查询快递员所属公司
     */
    public static final String URL_FIND_COMPANY_BY_HUMANID = MfhApi.URL_BASE_SERVER + "/stock/receiveBatch/findFdCompanyByHumanId";
    /**
     * 查询用户：/pmc/pmcstock/findHumanBySecret?secret=4645&stockId=1203,1207,1209
     */
    public static final String URL_STOCK_FINDHUMAN_BYSECRET = MfhApi.URL_BASE_SERVER + "/pmcstock/findHumanBySecret";
    /**
     * 快递入库，查询用户
     */
    public static final String URL_RECEIVE_ORDER_FIND_HUMANINFO_BY_MOBILE = MfhApi.URL_BASE_SERVER + "/stock/receiveOrder/findHumanInfoByMobile";


    /**
     * 代收快递创建批次
     */
    public static final String URL_RECEIVE_BATCH_CREATE_AND_FEE = MfhApi.URL_BASE_SERVER + "/stock/receiveBatch/createAndFee";
    /**
     * 在批次中录入具体快递明细
     */
    public static final String URL_STOCK_RECEIVEORDER_STOCKINITEMS = MfhApi.URL_BASE_SERVER + "/stock/receiveOrder/stockInItems";


    /**
     * 添加快递身份 /stock/receiveBatch/saveHumanFdCompany?humanId=0&companyId=0，
     */
    public static final String URL_RECEIVEBATCH_SAVE_HUMANFDCOMPANY = MfhApi.URL_BASE_SERVER + "/stock/receiveBatch/saveHumanFdCompany";


//    {"code":"0","msg":"查询成功!","version":"1","data":{"val":"{"deepType":"0","levelNum":"1",
// "options":[{"access":"0","code":731,"hasChild":"false","levelName":"","value":"玲珑湾精英公寓"},
// {"access":"0","code":732,"hasChild":"false","levelName":"","value":"尚玲珑"},{"access":"0",
// "code":733,"hasChild":"false","levelName":"","value":"加城花园"}]}"}}
    /**
     * 获取网点周边的小区 /shopSubdistRelation/findSubdisCodesByNetId?netId=133004 ，网点就是当前登录人员的curOffice，返回的是统一的编写型数据
     */
    public static final String URL_FIND_SUBDISCODES_BY_NETID = MfhApi.URL_BASE_SERVER + "/shopSubdistRelation/findSubdisCodesByNetId";


    /**
     * pos端核销卡券:
     * /mineCoupons/abandonCouponById?mineCps=19,20&orderId=1_110
     */
    public final static String URL_MIMECOUPONS_ABANDONCOUPON_BYID = MfhApi.URL_BASE_SERVER + "/mineCoupons/abandonCouponById";


    /**
     * 查询快递
     */
    public final static String URL_STOCK_RECEIVEBATCH_COMNQUERY = MfhApi.URL_BASE_SERVER + "/stock/receiveBatch/comnQuery";


    /**
     * 检查衣袋编号是否重复
     */
    public final static String URL_MFHORDER_CHECK_PACKAGECODE = MfhApi.URL_BASE_SERVER + "/mfhorder/isPackageCodeExist";

    /**
     * 检查衣袋编号是否重复
     */
    public final static String URL_MFHORDER_SAVE_LAUNDRYORDER = MfhApi.URL_BASE_SERVER + "/mfhorder/saveLaundryOrder";


    /**
     * 首次对某个临时用户发送手机短信验证码
     * /embWxUserRegister/beginAuthenBysms?mobile=&sourceType=10, 注意返回一个userTmpId备用
     */
    public final static String URL_BEGINAUTHENBYSMS = MfhApi.URL_BASE_SERVER +
            "/embWxUserRegister/beginAuthenBysms";

    /**
     * 对某个临时用户重新发送手机短信验证码
     * /embWxUserRegister/retryAuthenBysms?mobile=&userTmpId=
     */
    public final static String URL_RETRYAUTHENBYSMS = MfhApi.URL_BASE_SERVER +
            "/embWxUserRegister/retryAuthenBysms";

    /**
     * 对接收到的手机验证码进行验证。
     * /embWxUserRegister/doAuthenBysms?token=&userTmpId=
     */
    public final static String URL_DOAUTHENBYSMS = MfhApi.URL_BASE_SERVER +
            "/embWxUserRegister/doAuthenBysms";

    /**
     * 银联支付后调用后台  /payOrder/create?jsonStr={}
     */
    public final static String URL_PAYORDER_CREATE = MfhApi.URL_BASE_SERVER +
            "/payOrder/create";

    /**
     * 打款明细：/payOrder/list?status=2&sellOffice=136076&bizType=2001
     */
    public final static String URL_PAYORDER_LIST = MfhApi.URL_BASE_SERVER +
            "/payOrder/list";

    /**
     * 判断是否需要锁定pos，由pos端主动发起询问,/scNetRealInfo/needLockPos?netId=
     * */
    public final static String URL_NEEDLOCKPOS = MfhApi.URL_BASE_SERVER + "/scNetRealInfo/needLockPos";

    /**
     * 查询限额情况,第一个是限额（0代表没有设置或限额无穷大），第二个是未缴现金
     * /scNetRealInfo/queryLimitInfo
     * */
    public final static String URL_QUERYLIMITINFO = MfhApi.URL_BASE_SERVER + "/scNetRealInfo/queryLimitInfo";


    /**
     * 查pos订单的现金流水：
     * /orderPayWay/list?payType=1&officeId=136076&orderby=CREATED_DATE&orderbydesc=true
     * */
    public final static String URL_ORDERPAYWAY_LIST = MfhApi.URL_BASE_SERVER + "/orderPayWay/list";


}
