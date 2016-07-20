package com.manfenjiayuan.im;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 16/3/1.
 */
public class IMApi {
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
}
