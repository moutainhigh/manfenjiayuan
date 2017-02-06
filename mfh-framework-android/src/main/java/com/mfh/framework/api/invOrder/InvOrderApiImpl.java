package com.mfh.framework.api.invOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 4/22/16.
 */
public class InvOrderApiImpl extends InvOrderApi {

    /**
     * 查询历史报损列表
     */
    public static void queryInvLossOrderList(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
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
        params.put("posId", SharedPrefesManagerFactory.getTerminalId());
        params.put("jsonStr", jsonStr);
        AfinalFactory.getHttp(true).post(URL_INVLOSSORDERITEM_BATCHCOMMIT, params, responseCallback);
    }


}
