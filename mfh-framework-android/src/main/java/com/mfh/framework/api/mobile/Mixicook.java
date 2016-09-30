package com.mfh.framework.api.mobile;

import com.mfh.framework.BizConfig;

/**
 * Created by bingshanguxue on 9/29/16.
 */

public class Mixicook {
    public static String API_BASE_URL = "http://devmobile.mixicook.com/mobile/";
    public static String DOMAIN = "devmobile.mixicook.com";


    static{
        if(BizConfig.RELEASE){
            API_BASE_URL = "http://mobile.mixicook.com/mobile/";
//            BASE_URL_RESOURCE = "http://resource.manfenjiayuan.cn/user/";
            DOMAIN = "mobile.mixicook.com";
        }else{
            API_BASE_URL = "http://devmobile.mixicook.com/mobile/";
//            BASE_URL_RESOURCE = "http://devresource.manfenjiayuan.cn/user/";
            DOMAIN = "devmobile.mixicook.com";
        }
    }

    public final static String URL_ME_ACCOUNT = API_BASE_URL + "me/account.html";

}
