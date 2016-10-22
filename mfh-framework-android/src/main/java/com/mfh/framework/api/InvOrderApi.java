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
    private final static String URL_INVIOORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invIoOrderItem/";
    //拣货单

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
     * 查询批次流水明细
     */
    public final static String URL_INVIOORDERITEM_LIST = URL_INVIOORDER_ITEM + "list";




}
