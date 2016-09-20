package com.mfh.framework.api.account;

import com.mfh.framework.api.MfhApi;

/**
 * 用户Api
 * Created by bingshanguxue on 4/21/16.
 */
public class UserApi {

    /**会话是否有效*/
    public static final String URL_VALID_SESSION = MfhApi.URL_BASE_SERVER + "/isSessionValid";
    /**用户信息*/
    public final static String URL_MY_PROFILE = MfhApi.URL_BASE_SERVER + "/baseProfile/myProfile";
    /**修改登录密码*/
    public final static String URL_USER_UPDATE_LOGINPWD = MfhApi.URL_BASE_SERVER + "/sys/human/updateUserPassword";
    /**修改支付密码*/
    private final static String URL_USER_UPDATE_PAYPWD = MfhApi.URL_BASE_SERVER + "/commonuseraccount/changePwd";

    /**更新用户信息:昵称/性别*/
    public final static String URL_USER_REGISTERUSER = MfhApi.URL_BASE_SERVER + "/sys/human/registerUser";
    /**更新用户信息:昵称/性别*/
    public final static String URL_USER_UPDATE = MfhApi.URL_BASE_SERVER + "/sys/human/update";
    /**修改头像*/
    public final static String URL_USER_UPLOAD_HEAD= MfhApi.URL_BASE_SERVER + "/sys/human/uploadHumanHeadImg";

    /**查询快递员，查询用户：/pmc/sys/human/getHumanByIdentity?mobile=2123&humanId=31323*/
    public static final String URL_FIND_HUMAN_BY_IDENTITY = MfhApi.URL_BASE_SERVER + "/sys/human/getHumanByIdentity";

    /**
     * 由门店或管理员进行设置用户的默认服务网点:
     /customer/createParamDirect?param={humanId:12345, paramName:"defaultNet",paramValue:132079}
     * */
    public final static String URL_CUSTOMER_CREATEPARAMDIRECT = MfhApi.URL_BASE_SERVER + "/customer/createParamDirect";


    /**
     * pmc/sysCommonParam/getMyParamValue?paramName=defaultNet
     * */
    public final static String URL_GET_MYPARAMVALUE = MfhApi.URL_BASE_SERVER + "/sysCommonParam/getMyParamValue";


}
