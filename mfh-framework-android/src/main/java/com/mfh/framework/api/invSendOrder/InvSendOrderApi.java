package com.mfh.framework.api.invSendOrder;

import com.mfh.framework.api.MfhApi;

/**
 * 采购订单
 * Created by bingshanguxue on 8/4/16.
 */
public class InvSendOrderApi {
    public static String URL_INVSENDORDER = MfhApi.URL_BASE_SERVER + "/invSendOrder/";

    /**
     * <b>需要登录</b>
     * 由门店方创建一个采购单，需要登录,采购单的tenantId就是门店的tenantId
     * 适用场景：门店智能订货，标品订货，生鲜订货
     */
    static String URL_INVSENDORDER_ASK_SENDORDER = URL_INVSENDORDER + "askSendOrder";


    /**
     * 查询采销单，便于生成收发单。
     * /invSendOrder/list?netFlag=true&netId=&statuss=&status=
     * netFlag:是门店方还是供货方; netId:发货网点或收货网点;
     * statuss-查询多个状态；status-查询一个状态(0-初始 1-审核通过 2-部分发货收货 4-全部结束)
     */
    static String URL_INVSENDORDER_LIST = URL_INVSENDORDER + "list";
    /**
     * 获取一个配送单的明细信息
     * /invSendOrder/getById?id=&barcode=
     */
    static String URL_INVSENDORDER_GETBYID = URL_INVSENDORDER + "getById";
    /**
     * 门店人员对指定的采购配送单进行入库：
     * /invSendOrder/doReceive 若accountPassword为null代表先不支付;
     * /invSendOrder/doReceive?orderId=1
     */
    static String URL_INVSENDORDER_DORECEIVE = URL_INVSENDORDER + "doReceive";
    /**
     * 取消一个采购单
     * /invSendOrder/cancelOrder?id=
     */
    static String URL_INVSENDORDER_CANCEL= URL_INVSENDORDER + "cancelOrder";
    /**
     * 门店对客户的订购单进行收货确认并通知客户前来取货，并设置存放的货架号
     * /invSendOrder/receivePlanOrderOnNet?id=&rackNo=
     */
    static String URL_INVSENDORDER_RECEIVEPLANORDERONNET= URL_INVSENDORDER + "receivePlanOrderOnNet";
    /**
     * 创建预定单
     * /invSendOrder/receivePlanOrderOnNet?id=&rackNo=
     */
    static String URL_INVSENDORDER_CREATE_PLANORDER= URL_INVSENDORDER + "createPlanOrder";


}
