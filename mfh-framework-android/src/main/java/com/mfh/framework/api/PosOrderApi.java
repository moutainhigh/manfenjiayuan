package com.mfh.framework.api;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * POS订单
 * Created by bingshanguxue on 04/11/2016.
 */

public class PosOrderApi {
    static String URL_POSORDER = MfhApi.URL_BASE_SERVER + "/posOrder/";

    /**
     * pos端批量将未同步订单数据同步到云端，并修改库存
     */
    private static String URL_BATCHINORDERS = URL_POSORDER + "batchInOrders";

    public static void register() {
        URL_POSORDER = MfhApi.URL_BASE_SERVER + "/posOrder/";
        URL_BATCHINORDERS = URL_POSORDER + "batchInOrders";
    }

    /**
     * 提交收银订单
     */
    public static void batchInOrders(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(PosOrderApi.URL_BATCHINORDERS, params, responseCallback);
    }


}
