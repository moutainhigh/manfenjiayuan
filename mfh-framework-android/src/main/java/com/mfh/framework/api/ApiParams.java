package com.mfh.framework.api;

/**
 * Created by bingshanguxue on 18/11/2016.
 */

public interface ApiParams {
    String PARAM_KEY_JSESSIONID = "JSESSIONID";
    String PARAM_KEY_JSONSTR = "jsonStr";
    String PARAM_KEY_QUEUENAME = "queuename";

    /**登录参数*/
    String PARAM_KEY_USERNAME = "username";
    String PARAM_KEY_PASSWORD = "password";
    String PARAM_KEY_LOGIN_TYPE = "loginType";
    String PARAM_KEY_LOGIN_KIND = "loginKind";

    /**消息参数*/
    String PARAM_KEY_CHANNEL_ID = "channelid"; //渠道编号
    String PARAM_KEY_SOURCE_ID = "sourceid";
    String PARAM_KEY_SESSION_ID = "sessionid";
    String PARAM_KEY_CHANNEL_POINT_ID = "channelpointid";
    String PARAM_KEY_GUID = "guid";
    String PARAM_KEY_SUBDIS_ID = "subdisid";
    String PARAM_KEY_BUREAD_UID = "bureaduid";
    String PARAM_KEY_LASTUPDATE = "lastupdate";
    String PARAM_KEY_CREATE_GUID = "createguid";
    String PARAM_KEY_TYPE = "type";
    String PARAM_KEY_BIND = "bind";
    String PARAM_KEY_PIC_URL = "picUrl";

    String PARAM_VALUE_LOGIN_TYPE_PMC = "PMC";

}
