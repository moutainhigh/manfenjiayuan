package com.mfh.framework.api.impl;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 4/22/16.
 */
public class InvOrderApiImpl extends InvOrderApi {


    /**
     * 查询库存盘点订单列表
     */
    public static void queryInvCheckOrderList(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVCHECKORDER_LIST, params, responseCallback);
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
        AfinalFactory.getHttp(true).post(InvOrderApi.URL_INVCHECKORDER_GETCURRENTORDER, params, responseCallback);
    }

    /**
     *  盘点商品
     *  盘点时，若提交的条码和返回的条码不一致，说明是箱规码转换成明细码了
     *  */
    public static void invCheckOrderBatchCommitItems(Long orderId, String jsonStr, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("orderId", String.valueOf(orderId));
        params.put("posId", SharedPreferencesManager.getTerminalId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.getHttp(true).post(InvOrderApi.URL_INVCHECKORDER_BATCHCOMMITITEMS, params, responseCallback);
    }

    public static void invCheckOrderCancelOrder(String orderId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("orderId", orderId);

        AfinalFactory.getHttp(true).post(InvOrderApi.URL_INVCHECKORDER_CANCELORDER, params, responseCallback);
    }

    public static void invCheckOrderCreatelOrder(Long netId, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("netId", (netId == null ? "" : String.valueOf(netId)));

        AfinalFactory.getHttp(true).post(InvOrderApi.URL_INVCHECKORDER_CREATEORDER, params, responseCallback);
    }


    /**
     * 查询历史报损列表
     */
    public static void queryInvLossOrderList(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVLOSSORDER_LIST, params, responseCallback);
    }

    public static void invLossOrderGetCurrentOrder(Long netId, Integer storeType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("storeType", String.valueOf(storeType));
        params.put("netId", (netId == null ? "" : String.valueOf(netId)));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_INVLOSSORDER_GETCURRENTORDER, params, responseCallback);
    }

    public static void invLossOrderItemBatchCommit(Long orderId, String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        if (orderId != null) {
            params.put("orderId", String.valueOf(orderId));
        }
        params.put("posId", SharedPreferencesManager.getTerminalId());
        params.put("jsonStr", jsonStr);
        AfinalFactory.getHttp(true).post(URL_INVLOSSORDERITEM_BATCHCOMMIT, params, responseCallback);
    }


}
