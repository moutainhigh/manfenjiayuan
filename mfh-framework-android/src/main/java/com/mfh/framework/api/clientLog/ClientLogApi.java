package com.mfh.framework.api.clientLog;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 客户端日志
 * Created by bingshanguxue on 9/22/16.
 */

public class ClientLogApi {
    public static String URL_CLIENTLOG = MfhApi.URL_BASE_SERVER + "/clientLog/";


    /**
     * 日志列表
     */
    private static String URL_LIST = URL_CLIENTLOG + "list";


    public static void register() {
        URL_CLIENTLOG = MfhApi.URL_BASE_SERVER + "/clientLog/";
        URL_LIST = URL_CLIENTLOG + "list";
    }

    /**
     * 日志列表
     */
    public static void list(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
////
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_LIST, params, responseCallback);
    }
}
