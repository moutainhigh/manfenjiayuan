package com.mfh.enjoycity.wxapi;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 13/08/2017.
 */

public class WxUserInfo implements Serializable {
    private String openid;
    private String nickname;
    private int sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String unionid;//用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的unionid是唯一的。
}
