package com.mfh.framework.api.account;

import static com.mfh.framework.api.MfhApi.URL_BASE_SERVER;

/**
 * 用户Api
 * Created by bingshanguxue on 4/21/16.
 */
public class UserApi {
    /**登录*/
    public static String URL_LOGIN = URL_BASE_SERVER + "/login";
    /**退出*/
    public static String URL_EXIT= URL_BASE_SERVER + "/exit";
    /**会话是否有效*/
    public static String URL_VALID_SESSION = URL_BASE_SERVER + "/isSessionValid";
    /**用户信息*/
    public static String URL_MY_PROFILE = URL_BASE_SERVER + "/baseProfile/myProfile";
    /**修改登录密码*/
    public static String URL_USER_UPDATE_LOGINPWD = URL_BASE_SERVER + "/sys/human/updateUserPassword";
    /**更新用户信息:昵称/性别*/
    public static String URL_USER_REGISTERUSER = URL_BASE_SERVER + "/sys/human/registerUser";
    /**更新用户信息:昵称/性别*/
    public static String URL_USER_UPDATE = URL_BASE_SERVER + "/sys/human/update";
    /**修改头像*/
    public static String URL_USER_UPLOAD_HEAD= URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";

    /**查询快递员，查询用户：/pmc/sys/human/getHumanByIdentity?mobile=2123&humanId=31323*/
    public static String URL_FIND_HUMAN_BY_IDENTITY = URL_BASE_SERVER + "/sys/human/getHumanByIdentity";

    /**
     * 由门店或管理员进行设置用户的默认服务网点:
     /customer/createParamDirect?param={humanId:12345, paramName:"defaultNet",paramValue:132079}
     * */
    public static String URL_CUSTOMER_CREATEPARAMDIRECT = URL_BASE_SERVER + "/customer/createParamDirect";


    /**
     * pmc/sysCommonParam/getMyParamValue?paramName=defaultNet
     * */
    public static String URL_GET_MYPARAMVALUE = URL_BASE_SERVER + "/sysCommonParam/getMyParamValue";


    public static void register(){
        URL_LOGIN = URL_BASE_SERVER + "/login";
        URL_EXIT = URL_BASE_SERVER + "/exit";
        URL_VALID_SESSION = URL_BASE_SERVER + "/isSessionValid";
        URL_MY_PROFILE = URL_BASE_SERVER + "/baseProfile/myProfile";
        URL_USER_UPDATE_LOGINPWD = URL_BASE_SERVER + "/sys/human/updateUserPassword";
        URL_USER_REGISTERUSER = URL_BASE_SERVER + "/sys/human/registerUser";
        URL_USER_UPDATE = URL_BASE_SERVER + "/sys/human/update";
        URL_USER_UPLOAD_HEAD= URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";
        URL_FIND_HUMAN_BY_IDENTITY = URL_BASE_SERVER + "/sys/human/getHumanByIdentity";
        URL_CUSTOMER_CREATEPARAMDIRECT = URL_BASE_SERVER + "/customer/createParamDirect";
        URL_GET_MYPARAMVALUE = URL_BASE_SERVER + "/sysCommonParam/getMyParamValue";
    }
}
