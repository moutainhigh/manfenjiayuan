package com.mfh.framework.api.clientLog;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 客户端日志
 * Created by bingshanguxue on 9/22/16.
 */

public class ClientLogApi {
    private final static String URL_CLIENTLOG = MfhApi.URL_BASE_SERVER + "/clientLog/";


    /**
     * 创建日志
     * */
    private final static String URL_CREATE = URL_CLIENTLOG + "create";

    /**
     * 日志列表
     * */
    private final static String URL_LIST = URL_CLIENTLOG + "list";


    /**
     * 创建日志
     * */
    public static void create(ClientLog clientLog, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (clientLog != null){

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("stackInformation", clientLog.getStackInformation());
            jsonObject.put("hardwareInformation", clientLog.getHardwareInformation());
            jsonObject.put("androidLevel", clientLog.getAndroidLevel());
            jsonObject.put("loginName", clientLog.getLoginName());
            jsonObject.put("softVersion", clientLog.getSoftVersion());
            jsonObject.put("errorTime", TimeUtil.format(clientLog.getErrorTime(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));

            params.put("jsonStr", jsonObject.toJSONString());
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CREATE, params, responseCallback);
    }
    /**
     * 日志列表
     * */
    public static void list(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
////
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_LIST, params, responseCallback);
    }
}
