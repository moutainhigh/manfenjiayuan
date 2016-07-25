package com.mfh.framework.api.invIoOrder;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 出入库订单
 * Created by bingshanguxue on 7/22/16.
 */
public class InvIoOrderApi {
    private final static String URL_INVIOORDER = MfhApi.URL_BASE_SERVER + "/invIoOrder/";


    /**出入库类型：0-入库 1-出库 2-直接设置*/
    public static final int ORDER_TYPE_IN = 0;
    public static final int ORDER_TYPE_OUT = 1;
    public static final int ORDER_TYPE_SET = 2;

    /**商品的仓储类型，0-代表是零售 1-是批发*/
    public static final int STORE_TYPE_RETAIL = 1;
    public static final int STORE_TYPE_WHOLESALE = 2;

    /**
     * 查询出入库批次流水
     */
    public final static String URL_LIST = URL_INVIOORDER + "list";

    /**
     * 创建一个针对本网点仓库的出入库单
     * /invIoOrder/createIoOrder
     */
    public final static String URL_CREATE_IOORDER = URL_INVIOORDER + "createIoOrder";

    /**
     * 提交一个出入库单，正式出入库,如果是出库单可以进一步填写物流信息如车辆、司机（可空）。
     * /invIoOrder/commitOrder
     */
    public final static String URL_COMMIT_ORDER = URL_INVIOORDER + "commitOrder";


    /**
     * 查询出入库批次流水：出库/入库单
     *
     * @param orderType 出入库类型, 0|1|2  0-入库 1-出库 2-直接设置
     */
    public static void list(int orderType, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderType", String.valueOf(orderType));
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put("wrapper", "true");
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }

    /**
     * 查询批次流水：出库/入库单
     *
     * @param orderType 出入库类型, 0|1|2  0-入库 1-出库 2-直接设置
     * @param storeType 商品的仓储类型，0-代表是零售 1-是批发
     * @param items 明细
     */
    public static void createIoOrder(int orderType, int storeType,
                                     JSONArray items, AjaxCallBack<? extends Object> responseCallback) {
        JSONObject jsonStr = new JSONObject();
        jsonStr.put("orderType", orderType);
        jsonStr.put("storeType", storeType);
        jsonStr.put("items", items);

        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonStr.toJSONString());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_CREATE_IOORDER, params, responseCallback);
    }

    /**
     * 查询批次流水：出库/入库单
     *
     * @param orderId 订单编号
     * @param transHumanId 司机（可空）
     * @param vehicle 车辆
     */
    public static void commitOrder(String orderId, Long transHumanId, String vehicle,
                                   AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", orderId);
        if (transHumanId != null){
            params.put("transHumanId", String.valueOf(transHumanId));
        }
        params.put("vehicle", vehicle);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_COMMIT_ORDER, params, responseCallback);
    }
}
