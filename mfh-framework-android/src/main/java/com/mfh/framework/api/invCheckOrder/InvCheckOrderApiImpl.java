package com.mfh.framework.api.invCheckOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class InvCheckOrderApiImpl extends InvCheckOrderApi {
    /**
     * 查询库存盘点订单列表
     */
    public static void list(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }

    /**
     * 结束盘点
     */
    public static void finishInvCheckOrder(Long orderId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (orderId != null) {
            params.put("orderId", String.valueOf(orderId));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_FINISHORDER, params, responseCallback);
    }

    public static void invCheckOrderGetCurrentOrder(Long netId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("netId", (netId == null ? "" : String.valueOf(netId)));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_GETCURRENTORDER, params, responseCallback);
    }

    /**
     *  盘点商品
     *  盘点时，若提交的条码和返回的条码不一致，说明是箱规码转换成明细码了
     *  */
    public static void invCheckOrderBatchCommitItems(Long orderId, String jsonStr, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("orderId", String.valueOf(orderId));
        params.put("posId", SharedPrefesManagerFactory.getTerminalId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_BATCHCOMMITITEMS, params, responseCallback);
    }

    public static void invCheckOrderCancelOrder(String orderId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("orderId", orderId);

        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_CANCELORDER, params, responseCallback);
    }

    public static void invCheckOrderCreatelOrder(Long netId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("netId", (netId == null ? "" : String.valueOf(netId)));

        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_CREATEORDER, params, responseCallback);
    }

}
