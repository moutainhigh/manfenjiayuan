package com.mfh.framework.network;


import com.mfh.framework.MfhApplication;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.Date;

/**
 * Created by bingshanguxue on 2015/6/11.
 */
public class URLHelper {
    public static final String URL_KEY_T = "t";

    public static String append(String url, String key, String value){
        if(url == null){
            return "";
        }

        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")){//.indexOf("?") > 0
            sb.append("?");
        }else{
            sb.append("&");
        }
        sb.append(String.format("%s=%s", key, value));
        return sb.toString();
    }

    /**
     * @param baseUrl
     * @param keyValues keyValues
     * */
    public static String append(String baseUrl, String keyValues){
        if(baseUrl == null){
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);

        //统一URL参数格式
        //appid
        if(!baseUrl.contains("?")){
            sb.append("?");
        }else{
            sb.append("&");
        }
        sb.append(uniformFormat());

        //添加新参数
        if(keyValues != null){
            sb.append("&");
            sb.append(keyValues);
        }

        return sb.toString();
    }

    public static String uniformFormat(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("appid=%s", MfhApplication.getAppId()));

        //随机字符串
        sb.append("&");
        sb.append(String.format("t=%s", String.valueOf(new Date().getTime())));

        sb.append("&");
        sb.append(String.format("JSESSIONID=%s", MfhLoginService.get().getCurrentSessionId()));

        return sb.toString();
    }
}
