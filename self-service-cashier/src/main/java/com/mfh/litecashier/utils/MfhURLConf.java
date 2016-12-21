package com.mfh.litecashier.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.CashierApp;
import com.mfh.framework.BizConfig;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.comn.config.UConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * MFH · Android · URL
 * Created by Administrator on 2015/5/12.
 */
public class MfhURLConf {

    private static String BASE_URL_APP = "";

    static{
        if(BizConfig.RELEASE){
            BASE_URL_APP = NetFactory.getServerUrl(UConfig.CONFIG_COMMON, "app.h5.url");
        }else{
            BASE_URL_APP = NetFactory.getServerUrl(UConfig.CONFIG_COMMON, "dev.app.h5.url");
        }
    }

    private final static String URL_H5 = BASE_URL_APP + "/m/app.html";
    /**自采商品*/
    public final static String URL_COMMODITY_PURCHASE = BASE_URL_APP + "/app/demo/demo5.html";
    /**库存商品*/
    public final static String URL_COMMODITY_STOCK = BASE_URL_APP + "/app/demo/demo1.html";
    /**商品中心*/
    public final static String URL_COMMODITY_CENTER = "http://www.manfenjiayuan.cn/htm/p1/index.html";
    /**报表*/
    public final static String URL_REPORT = "http://www.manfenjiayuan.cn/htm/salesana.html";


    /**
     * 重新组合URL
     * */
    public static String generateUrl(String baseUrl, String params) {
        if (baseUrl == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);

        //统一URL参数格式
        if(!baseUrl.contains("?")){
            sb.append("?");
        }else{
            sb.append("&");
        }
        //appid
        sb.append(String.format("appid=%s", CashierApp.getAppId()));

//        //随机字符串
        sb.append("&");
        sb.append(String.format("t=%s", String.valueOf(new Date().getTime())));

        sb.append("&");
        sb.append(String.format("JSESSIONID=%s", MfhLoginService.get().getCurrentSessionId()));

        //添加新参数
        if (params != null) {
            sb.append("&");
            sb.append(params);
        }

        return sb.toString();
    }

    public static String generateUrl2(String baseUrl, String params){
        if(baseUrl == null){
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);

        //统一URL参数格式
        //appid

//        sb.append(String.format("appid=%s", com.mfh.comna.api.helper.AppHelper.getAppId()));

//        //随机字符串
//        sb.append("&");
//        sb.append(String.format("t=%s", String.valueOf(new Date().getTime())));

//        sb.append("&");
//        sb.append(String.format("JSESSIONID=%s", SharedPrefesManagerFactory.getLastSessionId()));

        //添加新参数
        if(params != null){
            if(!baseUrl.contains("?")){
            sb.append("?");
        }else{
            sb.append("&");
        }
            sb.append(params);
        }

        return sb.toString();
    }

    /**
     * 重新组合URL
     * */
    public static String generateH5Url(String baseUrl, String params){
        if(baseUrl == null){
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(URL_H5);

        //统一URL参数格式
        //appid
        if(!baseUrl.contains("?")){
            sb.append("?");
        }else{
            sb.append("&");
        }
        sb.append(String.format("appid=%s", CashierApp.getAppId()));

        sb.append("&");
        try {
            sb.append(String.format("redirect=%s", URLEncoder.encode(generateUrl2(baseUrl, params), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
            ZLogger.e(e.toString());
        }

        //随机字符串
        sb.append("&");
        sb.append(String.format("t=%s", String.valueOf(new Date().getTime())));

        //随机字符串
        sb.append("&");
        sb.append(String.format("tenantId=%d", MfhLoginService.get().getSpid()));

        //TODO
//        sb.append("&");
//        sb.append(String.format("channelid=%d", MfhApi.PARAM_VALUE_CHANNEL_ID_DEF));

        sb.append("&");
        sb.append(String.format("humanid=%s", MfhLoginService.get().getHumanId()));

        sb.append("&");
        sb.append(String.format("JSESSIONID=%s", MfhLoginService.get().getCurrentSessionId()));

        //添加新参数
        if(params != null){
            sb.append("&");
            sb.append(params);
        }

        return sb.toString();
    }

}
