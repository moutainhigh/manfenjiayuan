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

    /**
     * 查询订单列表 /pmcstock/findGoodsOrderList?orderStatus=4&btype=7
     */
    public static final String URL_STOCK_FIND_GOODS_ORDERLIST = MfhApi.URL_BASE_SERVER + "/pmcstock/findGoodsOrderList";

//    {"code":"0","msg":"查询成功!","version":"1","data":{"val":"{"deepType":"0","levelNum":"1",
// "options":[{"access":"0","code":731,"hasChild":"false","levelName":"","value":"玲珑湾精英公寓"},
// {"access":"0","code":732,"hasChild":"false","levelName":"","value":"尚玲珑"},{"access":"0",
// "code":733,"hasChild":"false","levelName":"","value":"加城花园"}]}"}}
    /**
     * 获取网点周边的小区 /shopSubdistRelation/findSubdisCodesByNetId?netId=133004 ，网点就是当前登录人员的curOffice，返回的是统一的编写型数据
     */
    public static final String URL_FIND_SUBDISCODES_BY_NETID = MfhApi.URL_BASE_SERVER + "/shopSubdistRelation/findSubdisCodesByNetId";


    /**
     * 当前登录人员即发货人员，自己进行发货 /scOrder/sendOrder?orderId=
     */
    public static final String URL_SCORDER_SENDORDER = MfhApi.URL_BASE_SERVER + "/scOrder/sendOrder";


    /**
     * pos端提交客户编号和订单基础信息获取可用卡券
     * /pmcstock/findConpousByOrderInfo?humanId=..&jsonStr={productId:[1,2,3], officeId:.., orderAmount:...}
     */
    public final static String URL_FIND_COUPONS_BY_ORDERINFO = MfhApi.URL_BASE_SERVER + "/pmcstock/findConpousByOrderInfo";

    /**
     * 查询卡券,订单提交前
     */
    public final static String URL_FIND_MARKETRULES_BY_ORDERINFO = MfhApi.URL_BASE_SERVER + "/pmcstock/findMarketRulesByOrderInfo";
    public final static String URL_FIND_MARKETRULES_BY_ORDERINFOS = MfhApi.URL_BASE_SERVER + "/pmcstock/findMarketRulesByOrderInfos";


    /**
     * pos端提交客户编号、订单基础信息和卡券信息，计算金额
     */
    public final static String URL_GET_PAYAMOUNT_BY_ORDERINFO = MfhApi.URL_BASE_SERVER + "/commonuseraccount/getPayAmountByOrderInfo";
    public final static String URL_GET_PAYAMOUNT_BY_ORDERINFOS = MfhApi.URL_BASE_SERVER + "/commonuseraccount/getPayAmountByOrderInfos";

    /**
     * pos端核销卡券:
     * /mineCoupons/abandonCouponById?mineCps=19,20&orderId=1_110
     */
    public final static String URL_MIMECOUPONS_ABANDONCOUPON_BYID = MfhApi.URL_BASE_SERVER + "/mineCoupons/abandonCouponById";

    /**
     * 查询子账号
     * /companyHuman/findCompUserPwdInfo?page=-1&rows=
     */
    public final static String URL_COMPANYHUMAN_FIND_COMPUSERPWDINFO = MfhApi.URL_BASE_SERVER + "/companyHuman/findCompUserPwdInfo";
    /**
     * 查询部门子账号
     * /pmc/companyHuman/listWorkerBeanInfoOfCompany?page=-1&rows=
     */
    public final static String URL_COMPANYHUMAN_LIST_WORKERBEANINFO_OFCOMPANY = MfhApi.URL_BASE_SERVER + "/companyHuman/listWorkerBeanInfoOfCompany";

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
     * 对接收到的手机验证码进行验证。
     * /embWxUserRegister/doAuthenBysms?token=&userTmpId=
     */
    public final static String URL_DOAUTHENBYSMS = MfhApi.URL_BASE_SERVER +
            "/embWxUserRegister/doAuthenBysms";

    /**
     * 对某个临时用户重新发送手机短信验证码
     * /embWxUserRegister/retryAuthenBysms?mobile=&userTmpId=
     */
    public final static String URL_RETRYAUTHENBYSMS = MfhApi.URL_BASE_SERVER +
            "/embWxUserRegister/retryAuthenBysms";

    /**
     * 开卡并激活用户账户  /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
     */
    public final static String URL_ACTIVATEACCOUNT = MfhApi.URL_BASE_SERVER +
            "/commonuseraccount/activateAccount";

    /**
     * 会员卡充值:给其他帐号转账  /commonuseraccount/activateAccount?cardId=334455667788&ownerId=94182
     */
    public final static String URL_TRANSFERFROMMYACCOUNT = MfhApi.URL_BASE_SERVER +
            "/commonuseraccount/transferFromMyAccount";


    /**
     * 银联支付后调用后台  /payOrder/create?jsonStr={}
     */
    public final static String URL_PAYORDER_CREATE = MfhApi.URL_BASE_SERVER +
            "/payOrder/create";


    /**
     * 判断是否需要锁定pos，由pos端主动发起询问,/scNetRealInfo/needLockPos?netId=
     * */
    public final static String URL_NEEDLOCKPOS = MfhApi.URL_BASE_SERVER + "/scNetRealInfo/needLockPos";


}
