package com.mfh.framework.api.invIoOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 出入库订单
 * Created by bingshanguxue on 7/22/16.
 */
public class InvIoOrderApi {
    public static String URL_INVIOORDER = MfhApi.URL_BASE_SERVER + "/invIoOrder/";


    /**
     * 出入库类型：0-入库 1-出库 2-直接设置
     */
    public static final int ORDER_TYPE_IN = 0;
    public static final int ORDER_TYPE_OUT = 1;
    public static final int ORDER_TYPE_SET = 2;

    /**
     * 商品的仓储类型，0-代表是零售 1-是批发
     */
    public static final int STORE_TYPE_RETAIL = 0;
    public static final int STORE_TYPE_WHOLESALE = 1;

    /**
     * 查询出入库批次流水
     */
    public static String URL_LIST = URL_INVIOORDER + "list";

    public static void register() {
        URL_INVIOORDER = MfhApi.URL_BASE_SERVER + "/invIoOrder/";
        URL_LIST = URL_INVIOORDER + "list";
    }

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

}
