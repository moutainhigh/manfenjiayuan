package com.mfh.framework.api.invOrder;

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
