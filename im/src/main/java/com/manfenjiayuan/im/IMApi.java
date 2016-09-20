package com.manfenjiayuan.im;

import com.alibaba.fastjson.JSON;
import com.manfenjiayuan.im.utils.IMFactory;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import static com.mfh.framework.api.MfhApi.CHANNEL_ID;
import static com.mfh.framework.api.MfhApi.PARAM_KEY_CHANNEL_ID;
import static com.mfh.framework.api.MfhApi.PARAM_KEY_JSESSIONID;
import static com.mfh.framework.api.MfhApi.PARAM_KEY_JSONSTR;
import static com.mfh.framework.api.MfhApi.PARAM_KEY_QUEUE_NAME;
import static com.mfh.framework.api.MfhApi.PARAM_VALUE_QUEUE_NAME_DEF;

/**
 * Created by bingshanguxue on 16/3/1.
 */
public class IMApi {
    //注册消息桥
    public static String URL_MOBILE_MESSAGE = NetFactory.getRegisterMessageUrl();
    //会话
    public static String URL_GET_SESSION_BY_ID = MfhApi.URL_BASE_SERVER + "/biz/msg/getSessionById";

    //网络请求参数关键字
    public static final String PARAM_KEY_SOURCE_ID = "sourceid";
    public static final String PARAM_KEY_SESSION_ID = "sessionid";
    public static final String PARAM_KEY_CHANNEL_POINT_ID = "channelpointid";
    public static final String PARAM_KEY_GUID = "guid";
    public static final String PARAM_KEY_JSON_STR = "jsonStr";
    public static final String PARAM_KEY_SUBDIS_ID = "subdisid";
    public static final String PARAM_KEY_BUREAD_UID = "bureaduid";
    public static final String PARAM_KEY_LASTUPDATE = "lastupdate";
    public static final String PARAM_KEY_CREATE_GUID = "createguid";
    public static final String PARAM_KEY_TYPE = "type";
    public static final String PARAM_KEY_BIND = "bind";
    public static final String PARAM_KEY_PIC_URL = "picUrl";


    /**
     * 注册消息桥
     * @param guid
     * @param clientId
     * @param responseCallback
     * */
    public static void registerBridge(Long guid, String clientId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_CHANNEL_ID, CHANNEL_ID);
        params.put(PARAM_KEY_QUEUE_NAME, PARAM_VALUE_QUEUE_NAME_DEF);
        params.put(PARAM_KEY_JSONSTR, JSON.toJSONString(IMFactory.register(clientId, guid)));
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_MOBILE_MESSAGE, params, responseCallback);
    }
}
