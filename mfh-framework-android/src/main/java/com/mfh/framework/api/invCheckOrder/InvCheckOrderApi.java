package com.mfh.framework.api.invCheckOrder;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class InvCheckOrderApi {
    public static String URL_INVCHECKORDER = MfhApi.URL_BASE_SERVER + "/invCheckOrder/";
    public static String URL_INVCHECKORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invCheckOrderItem/";

    /**
     * 获取当前网点正在盘点的盘点单号和名称
     */
    public static String URL_INVCHECKORDER_GETCURRENTORDER = URL_INVCHECKORDER + "getCurrentOrder";

    /**
     * 如果没有盘点单，创建一个新的盘点单，返回盘点单号和名称
     * /invCheckOrder/createCurrentOrder
     */
    public static String URL_INVCHECKORDER_CREATEORDER = URL_INVCHECKORDER + "createCurrentOrder";

    /**
     * PC端取消一个盘点单
     * /invCheckOrder/cacelOrder?orderId=
     */
    public static String URL_INVCHECKORDER_CANCELORDER = URL_INVCHECKORDER + "cacelOrder";

    /**
     * 库存盘点订单列表
     */
    public static String URL_LIST = URL_INVCHECKORDER + "list";
    /**
     * 结束盘点
     */
    public static String URL_INVCHECKORDER_FINISHORDER = URL_INVCHECKORDER + "finishOrder";


    /**
     * 库存盘点订单明细列表
     */
    public static String URL_INVCHECKORDERITEM_LIST = URL_INVCHECKORDER_ITEM + "list";

    /**
     * 盘点机针对一个盘点单，提交一批盘点记录,需要登录。
     * /invCheckOrderItem/batchCommitItems?orderId=&posId=&jsonStr=[{"barcode":"9999999999995","quantityCheck":220, "updateHint":0}, {"barcode":"9999999999994","quantityCheck":150, "updateHint":0}]
     */
    public static String URL_INVCHECKORDER_BATCHCOMMITITEMS = URL_INVCHECKORDER_ITEM + "batchCommitItems";

    public static void register(){
        URL_INVCHECKORDER = MfhApi.URL_BASE_SERVER + "/invCheckOrder/";
        URL_INVCHECKORDER_GETCURRENTORDER = URL_INVCHECKORDER + "getCurrentOrder";
        URL_INVCHECKORDER_CREATEORDER = URL_INVCHECKORDER + "createCurrentOrder";
        URL_INVCHECKORDER_CANCELORDER = URL_INVCHECKORDER + "cacelOrder";
        URL_LIST = URL_INVCHECKORDER + "list";
        URL_INVCHECKORDER_FINISHORDER = URL_INVCHECKORDER + "finishOrder";

        URL_INVCHECKORDER_ITEM = MfhApi.URL_BASE_SERVER + "/invCheckOrderItem/";
        URL_INVCHECKORDERITEM_LIST = URL_INVCHECKORDER_ITEM + "list";
        URL_INVCHECKORDER_BATCHCOMMITITEMS = URL_INVCHECKORDER_ITEM + "batchCommitItems";

    }

}
