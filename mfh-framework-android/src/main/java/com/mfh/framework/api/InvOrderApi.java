package com.mfh.framework.api;

/**
 * 库存订单Api
 * Created by bingshanguxue on 4/22/16.<br>
 * <ol>
 * <li>/invCheckOrder</li>
 * </ol>
 */
public class InvOrderApi {
    public final static Integer BIZTYPE_PURCHASE            = 0;//采购
    public final static Integer BIZTYPE_TRANSFER            = 1;//调拨

    public final static Integer SENDSTORE_TYPE_RETAIL       = 0;//零售仓
    public final static Integer SENDSTORE_TYPE_WHOLESALE    = 1;//批发仓

    public static final Integer ORDERTYPE_RECEIPT           = 0;//正常收发
    public static final Integer ORDERTYPE_RETURN            = 1;//退货

    //支付状态
    public static final Integer PAY_STATUS_NOT_PAID         = 0;
    public static final Integer PAY_STATUS_PAID             = 1;


    public static final String PO = "1";//生鲜预定
    public static final String PI = "2";//发货单

    /**
     * 订单状态
     * */
    public static final Integer ORDER_STATUS_INIT     = 0;//生成
    public static final Integer ORDER_STATUS_CONFIRM  = 1;//审核通过
    public static final Integer ORDER_STATUS_SENDED   = 2;//已发货
    public static final Integer ORDER_STATUS_ON_TRANS = 3;//在途中
    public static final Integer ORDER_STATUS_RECEIVE  = 4;//已签收
    public static final Integer ORDER_STATUS_CANCEL   = 9;//已取消

    public static String orderStatusCaption(Integer value) {
        if (value.equals(ORDER_STATUS_INIT)) {
            return "生成";
        }
        else if (value.equals(ORDER_STATUS_CONFIRM)) {
            return "审核通过";
        }
        else if (value.equals(ORDER_STATUS_SENDED)) {
            return "已发货";
        }
        else if (value.equals(ORDER_STATUS_ON_TRANS)) {
            return "在途中";
        }
        else if (value.equals(ORDER_STATUS_RECEIVE)) {
            return "已签收";
        }
        else if (value.equals(ORDER_STATUS_CANCEL)) {
            return "已取消";
        }
        else{
            return "Unknow";
        }
    }


    /**
     * 订单类型
     * */
    public static final Integer SENDTYPE_SUPERMARKET_AUTO   = 0;//门店自动建单
    public static final Integer SENDTYPE_SUPERMARKET_MANUAL = 1;//门店人工建单
    public static final Integer SENDTYPE_WHOLESALER_MANUAL  = 2;//批发商建采购单
    public static final Integer SENDTYPE_CUSTOMER_MANUAL    = 3;//客户建单


    private final static String URL_INVCHECKORDER = MfhApi.URL_BASE_SERVER + "/invCheckOrder/";
    private final static String URL_INVCHECKORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invCheckOrderItem/";
    private final static String URL_INVLOSSORDER = MfhApi.URL_BASE_SERVER + "/invLossOrder/";
    private final static String URL_INVLOSSORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invLossOrderItem/";

    //收发单：收货单&发货单
    private final static String URL_INVSENDIOORDER = MfhApi.URL_BASE_SERVER + "/invSendIoOrder/";
    private final static String URL_INVIOORDER = MfhApi.URL_BASE_SERVER + "/invIoOrder/";
    private final static String URL_INVIOORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invIoOrderItem/";
    //拣货单
    private final static String URL_INVFINDORDER = MfhApi.URL_BASE_SERVER + "/invFindOrder/";

    /**
     * 获取当前网点正在盘点的盘点单号和名称
     */
    public final static String URL_INVCHECKORDER_GETCURRENTORDER = URL_INVCHECKORDER + "getCurrentOrder";

    /**
     * 如果没有盘点单，创建一个新的盘点单，返回盘点单号和名称
     * /invCheckOrder/createCurrentOrder
     */
    public final static String URL_INVCHECKORDER_CREATEORDER = URL_INVCHECKORDER + "createCurrentOrder";

    /**
     * PC端取消一个盘点单
     * /invCheckOrder/cacelOrder?orderId=
     */
    public final static String URL_INVCHECKORDER_CANCELORDER = URL_INVCHECKORDER + "cacelOrder";

    /**
     * 库存盘点订单列表
     */
    public final static String URL_INVCHECKORDER_LIST = URL_INVCHECKORDER + "list";
    /**
     * 结束盘点
     */
    public final static String URL_INVCHECKORDER_FINISHORDER = URL_INVCHECKORDER + "finishOrder";


    /**
     * 库存盘点订单明细列表
     */
    public final static String URL_INVCHECKORDERITEM_LIST = URL_INVCHECKORDER_ITEM + "list";

    /**
     * 盘点机针对一个盘点单，提交一批盘点记录,需要登录。
     * /invCheckOrderItem/batchCommitItems?orderId=&posId=&jsonStr=[{"barcode":"9999999999995","quantityCheck":220, "updateHint":0}, {"barcode":"9999999999994","quantityCheck":150, "updateHint":0}]
     */
    public final static String URL_INVCHECKORDER_BATCHCOMMITITEMS = URL_INVCHECKORDER_ITEM + "batchCommitItems";


    /**
     * 库存报损订单列表
     */
    public final static String URL_INVLOSSORDER_LIST = URL_INVLOSSORDER + "list";
    /**
     * 获取当前网点正在报损的报损单号
     * /invLossOrder/getCurrentOrder
     */
    public final static String URL_INVLOSSORDER_GETCURRENTORDER = URL_INVLOSSORDER + "getCurrentOrder";
    /**
     * 库存报损订单明细列表
     */
    public final static String URL_INVLOSSORDERITEM_LIST = URL_INVLOSSORDER_ITEM + "list";


    /**
     * 针对一个报损单，提交一批报损记录,需要登录。
     * /invLossOrderItem/batchCommitItems?orderId=21&posId=1&jsonStr=
     * [{"barcode":"6925303770594","quantityCheck":11, "updateHint":1}, {"barcode":"6921168509256","quantityCheck":11, "updateHint":1}]
     */
    public final static String URL_INVLOSSORDERITEM_BATCHCOMMIT = URL_INVLOSSORDER_ITEM + "batchCommitItems";



    /**
     * 创建正常发货单（/invSendIoOrder/create 用于发起独立门店间调拨，其实相当于销售供货）
     */
    public final static String URL_INVSENDIOORDER_CREATE = URL_INVSENDIOORDER + "create";


    /**
     * 由门店自行创建收货单（此时因批发商不在平台上故没办法发货）
     * /invSendIoOrder/createRecOrder?otherOrderId=&checkOk=true&jsonStr={"receiveNetId":132079, "tenantId":130222, "sendTenantId":222, items:[{"chainSkuId":111,"proSkuId":1264,"productName":"狗粮", "giveCount":0,"totalCount":2, "price":11, "amount":22, "barcode":"32131234524"}, {"proSkuId":1266,"productName":"蒜泥生菜", "totalCount":3, "price":5, "amount":15, "barcode":"qu75745555"}]}
     * 其中otherOrderId为采购单，此处可以为空;
     * checkOk代表是否直接自动审核通过并生成入库单,进一步地如果收货网点就是当前操作用户的登录网点，则还会自动对生成的入库单进行入库操作。
     * sendNetId参数不要传递，sendTenantId可以从采购单中拷贝，本身也可以置空；
     * receiveNetId、tenantId可以从采购单中拷贝，没有采购单则可以置空，若置空后台也会自动根据当前登录用户的部门和租户进行填充。
     * items是发货单明细，字段与采购单明细字段基本一致，从采购单中拷贝即可，其中giveCount是赠送数量。没有采购单时需要从头选择商品。
     */
    public final static String URL_INVSENDIOORDER_CREATE_RECORDER = URL_INVSENDIOORDER + "createRecOrder";


    /**
     * 由收货方创建一个退货单
     * /invSendIoOrder/createBackOrder?otherOrderId=&checkOk=true&transHumanId=&vehicle=&jsonStr={"receiveNetId":111, "tenantId":111,items:[{}]}
     * 其中receiveNetId和tenantId是退货收货方的网点和租户，可以为空；若为空系统后台自动从otherOrderId对应的收发单中读取。
     */
    public final static String URL_INVSENDIOORDER_CREATE_BACKORDER = URL_INVSENDIOORDER + "createBackOrder";
    /**
     * 由发货方创建调拨单
     * /invSendIoOrder/createTransOrder?checkOk=true&transHumanId=&vehicle=&jsonStr={"sendNetId":11, "receiveNetId":132079, "sendTenantId":222, items:[{"chainSkuId":280,"proSkuId":1264,"productName":"狗粮", "giveCount":0,"quantityCheck":2, "price":11, "amount":22, "barcode":"32131234524"}, {"chainSkuId":281,"proSkuId":1266,"productName":"蒜泥生菜", "quantityCheck":3, "price":5, "amount":15, "barcode":"qu75745555"}]}
     * 明细中的chainSkuId可能是批发仓中的商品sku编号，也可能是零售仓中的sku编号。
     * 其中sendNetId、sendTenantId可以为空，若为空则使用当前登录用户的默认信息。
     */
    public final static String URL_INVSENDIOORDER_CREATE_TRANSORDER = URL_INVSENDIOORDER + "createTransOrder";

    /**
     * 获查询一个收发单（收货单或发货单）及其明细的详情一个收发单详情
     *  /invSendIoOrder/getById?id=&barcode=
     */
    public final static String URL_INVSENDIOORDER_GETBYID = URL_INVSENDIOORDER + "getById";
    /**
     * 查询收发单（收货单或发货单）
     * /invSendIoOrder/list?wrapper=true&bizType=0&orderType=0&sendNetId=&receiveNetId=&statuss=&status=&payStatus=&barcode=
     * 其中bizType=0代表采购业务，orderType=0代表正常收发;
     * sendNetId-发货网点
     * receiveNetId-收货网点;
     * sendTenantId-发货网点所属租户
     * tenantId-收货网点所属租户
     * barcode-单据条码号
     * payStatus-支付状态
     * statuss-查询多个状态；status-查询一个状态(0-生成 1-审核通过 2-已发货 3-在途中 4-已签收 9-已取消)
     */
    public final static String URL_INVSENDIOORDER_LIST = URL_INVSENDIOORDER + "list";
    /**
     * 对指定的发货单或收货单进行审核确认操作,若当前操作人员所属网点与单据的发货网点或收货网点一致，还会自动进行出库或入库操作。/invSendIoOrder/doConfirm?id=1
     */
    public final static String URL_INVSENDIOORDER_DOCONFIRM = URL_INVSENDIOORDER + "doConfirm";
    /**
     * 门店人员对指定的收货单(批发商发货过来的)进行签收,同时可以进行支付；同时生成入库单进行入库。
     * 此时有多少货收多少货，实际数量不对也得收；有不对的后面按退货或报损流程处理。
     * /invSendIoOrder/doReceive?orderId=203&accountPassword=
     */
    public final static String URL_INVSENDIOORDER_DORECEIVE = URL_INVSENDIOORDER + "doReceive";

    /**
     * 门店人员对指定的采购配送单手工执行支付：
     * /invSendOrder/doPay?orderId=19&accountPassword=
     */
    public final static String URL_INVSENDIOORDER_DOPAY = URL_INVSENDIOORDER + "doPay";

    /**
     * 查询批次流水
     */
    public final static String URL_INVIOORDER_LIST = URL_INVIOORDER + "list";
    /**
     * 查询批次流水明细
     */
    public final static String URL_INVIOORDERITEM_LIST = URL_INVIOORDER_ITEM + "list";

    /**
     * 根据拣货单编号或条码检索一个拣货单及其所有明细
     *  /invFindOrder/getById?id=|barcode=451201106160530006
     *  注意拣货单的条码是4开头
     */
    public final static String URL_INVFINDORDER_GETBYID = URL_INVFINDORDER + "getById";


}
