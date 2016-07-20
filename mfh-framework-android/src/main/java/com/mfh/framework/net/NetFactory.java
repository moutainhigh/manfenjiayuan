package com.mfh.framework.net;

import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.configure.UConfigHelper;
import com.mfh.framework.core.logger.ZLogger;

import org.apache.http.cookie.Cookie;

/**
 * 服务器网络连接工厂
 * 
 * @author zhangyz created on 2013-5-15
 * @since Framework 1.0
 */
public class NetFactory {
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_cookie = "cookie";
    public static final String KEY_JSESSIONID = "JSESSIONID";//传给服务器的会话Id
    public static final String CHARSET_UTF8 = "utf-8";
    public static final int TIMEOUT = 30000;


    public final static String URL_REGISTER_MESSAGE = "app.message.url";

//    private static FinalHttp fh = null;//fh可以被多线程同时使用
    public static Cookie cookie;
    
//    /**
//     * 获取http连接
//     * @return
//     * @author zhangyz created on 2013-5-15
//     */
//    public static FinalHttp getHttp() {
//        return getHttp(MfhLoginService.get().getCurrentSessionId());
//    }
//
//    public static FinalHttp getHttp(boolean bAttachCookie) {
//        if (bAttachCookie){
//            return getHttp(MfhLoginService.get().getCurrentSessionId());
//        }else{
//            return getHttp(null);
//        }
//    }
//
//    /**
//     * @param sessionId 会话id
//     * */
//    public static FinalHttp getHttp(String sessionId) {
//        if (fh == null) {
//            synchronized (FinalHttp.class) {
//                if (fh == null) {
//                    fh = new FinalHttp();
//                    //必要的配置工作,以后要从配置文件中读取
//                    fh.configCharset(CHARSET_UTF8);
//                    fh.configTimeout(TIMEOUT);
//                    fh.configRequestExecutionRetryCount(0);//0代表无需重试。
//                }
//            }
//        }
//
//        //为空的情况，需要移除header中的cookie
//        if (StringUtils.isEmpty(sessionId)){
//            fh.removeHeader(HEADER_SET_COOKIE);
//            fh.removeHeader(HEADER_COOKIE);
//            fh.removeHeader(HEADER_cookie);
//        }else{
//            String cookie = String.format("%s=%s", KEY_JSESSIONID, sessionId);
//            fh.addHeader(HEADER_SET_COOKIE, cookie);
//            fh.addHeader(HEADER_COOKIE, cookie);
//            fh.addHeader(HEADER_cookie, cookie);
//        }
//
//        return fh;
//    }

    /**
     * 获取服务器，不以/结尾
     * 带参数，上面那个方法不带参，属于默认的只有一个配置文件的方法，
     * 本方法适用于一个以上的配置文件
     * @paam common
     * @param key
     * @return
     */
    public static String getServerUrl(String domain, String key){
        String serverUrl = UConfigHelper.getInstance().getDomainString(domain, key);
        if (serverUrl != null && serverUrl.endsWith("/")){
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }

        ZLogger.d(String.format("getServerUrl:<%s><%s>", key, serverUrl));
        return serverUrl;
    }

    /**
     * 获取服务器url地址，不以/结尾。
     * @return
     */
    public static String getServerUrl() {
        if(BizConfig.RELEASE){
            return getServerUrl(UConfig.CONFIG_COMMON, UConfig.CONFIG_PARAM_SERVERURL);
        }else{
            return getServerUrl(UConfig.CONFIG_COMMON, "dev." + UConfig.CONFIG_PARAM_SERVERURL);
        }
    }
    public static String getServerUrl(String key) {
        if(BizConfig.RELEASE){
            return getServerUrl(UConfig.CONFIG_COMMON, key);
        }else{
            return getServerUrl(UConfig.CONFIG_COMMON, "dev." + key);
        }
    }

    /**
     * 获取升级地址，不以/结尾
     * @return
     */
    public static String getUpdateServerUrl() {
        if(BizConfig.RELEASE){
            return getServerUrl(UConfig.CONFIG_COMMON, UConfig.CONFIG_PARAM_UPDATEURL);
        }else{
            return getServerUrl(UConfig.CONFIG_COMMON, "dev." + UConfig.CONFIG_PARAM_UPDATEURL);
        }
    }

    public static String getRegisterMessageUrl(){
        if(BizConfig.RELEASE){
            return NetFactory.getServerUrl(UConfig.CONFIG_COMMON, URL_REGISTER_MESSAGE);
        }else{
            return NetFactory.getServerUrl(UConfig.CONFIG_COMMON, "dev." + URL_REGISTER_MESSAGE);
        }
    }

    /**
     * 获取上传图片的URL
     * @return
     */
    public static String getImageUploadUrl() {
        return getServerUrl(UConfig.CONFIG_COMMON, UConfig.CONFIG_PARAM_IMAGE_UPLOAD);
    }

    /**
     *  获取渠道编号
     *  */
    public static String getChannelId(){
        String channelId = UConfigHelper.getInstance().getDomainString(UConfig.CONFIG_COMMON, "channel.id");
//        if (StringUtils.isEmpty(channelId)){
//            channelId = String.valueOf(MfhApi.PARAM_VALUE_CHANNEL_ID_DEF);
//        }
        return channelId;
    }

    /**
     *  获取微信支付渠道编号
     *  */
    public static String getWxPayChannelId(){
        String channelId = UConfigHelper.getInstance().getDomainString(UConfig.CONFIG_COMMON, "wxpay_chId");
//        if (StringUtils.isEmpty(channelId)){
//            channelId = String.valueOf(MfhApi.PARAM_VALUE_CHANNEL_ID_DEF);
//        }
        return channelId;
    }
    /**
     *  获取支付宝支付渠道编号
     *  */
    public static String getAliPayChannelId(){
        String channelId = UConfigHelper.getInstance().getDomainString(UConfig.CONFIG_COMMON, "alipay_chId");
//        if (StringUtils.isEmpty(channelId)){
//            channelId = String.valueOf(MfhApi.PARAM_VALUE_CHANNEL_ID_DEF);
//        }
        return channelId;
    }

}
