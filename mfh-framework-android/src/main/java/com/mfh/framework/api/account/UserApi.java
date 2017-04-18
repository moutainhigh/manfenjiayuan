package com.mfh.framework.api.account;

import com.mfh.framework.api.MfhApi;


/**
 * 用户Api
 * Created by bingshanguxue on 4/21/16.
 */
public class UserApi {
    /**登录*/
    public static String URL_LOGIN = MfhApi.URL_BASE_SERVER + "/login";
    /**退出*/
    public static String URL_EXIT= MfhApi.URL_BASE_SERVER + "/exit";
    /**用户信息*/
    public static String URL_MY_PROFILE = MfhApi.URL_BASE_SERVER + "/baseProfile/myProfile";
    /**更新用户信息:登录密码*/
    public static String URL_USER_UPDATE_LOGINPWD = MfhApi.URL_BASE_SERVER + "/sys/human/updateUserPassword";
    /**更新用户信息:昵称/性别*/
    public static String URL_USER_UPDATE = MfhApi.URL_BASE_SERVER + "/sys/human/update";
    /**更新用户信息:头像*/
    public static String URL_USER_UPLOAD_HEAD= MfhApi.URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";
    /**
     * 获取默认参数
     * pmc/sysCommonParam/getMyParamValue?paramName=defaultNet
     * */
    public static String URL_GET_MYPARAMVALUE = MfhApi.URL_BASE_SERVER + "/sysCommonParam/getMyParamValue";

    /**
     * 由门店或管理员进行设置用户的默认服务网点:
     /customer/createParamDirect?param={humanId:12345, paramName:"defaultNet",paramValue:132079}
     * */
    public static String URL_CUSTOMER_CREATEPARAMDIRECT = MfhApi.URL_BASE_SERVER + "/customer/createParamDirect";


    public static void register(){
        URL_LOGIN = MfhApi.URL_BASE_SERVER + "/login";
        URL_EXIT = MfhApi.URL_BASE_SERVER + "/exit";
        URL_MY_PROFILE = MfhApi.URL_BASE_SERVER + "/baseProfile/myProfile";
        URL_USER_UPDATE_LOGINPWD = MfhApi.URL_BASE_SERVER + "/sys/human/updateUserPassword";
        URL_USER_UPDATE = MfhApi.URL_BASE_SERVER + "/sys/human/update";
        URL_USER_UPLOAD_HEAD= MfhApi.URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";
        URL_CUSTOMER_CREATEPARAMDIRECT = MfhApi.URL_BASE_SERVER + "/customer/createParamDirect";
        URL_GET_MYPARAMVALUE = MfhApi.URL_BASE_SERVER + "/sysCommonParam/getMyParamValue";
    }
}
