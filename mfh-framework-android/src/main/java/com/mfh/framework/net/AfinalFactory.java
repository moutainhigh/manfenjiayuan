package com.mfh.framework.net;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/11/16.
 */
public class AfinalFactory extends NetFactory{
    private static FinalHttp fh = null;//fh可以被多线程同时使用

    /**
     * 获取http连接
     * @return
     * @author zhangyz created on 2013-5-15
     */
    public static FinalHttp getHttp() {
        return getHttp(MfhLoginService.get().getCurrentSessionId());
    }

    public static FinalHttp getHttp(boolean bAttachCookie) {
        if (bAttachCookie){
            return getHttp(MfhLoginService.get().getCurrentSessionId());
        }else{
            return getHttp(null);
        }
    }

    /**
     * @param sessionId 会话id
     * */
    public static FinalHttp getHttp(String sessionId) {
        if (fh == null) {
            synchronized (FinalHttp.class) {
                if (fh == null) {
                    fh = new FinalHttp();
                    //必要的配置工作,以后要从配置文件中读取
                    fh.configCharset(CHARSET_UTF8);
                    fh.configTimeout(TIMEOUT);
                    fh.configRequestExecutionRetryCount(0);//0代表无需重试。
                }
            }
        }

        //为空的情况，需要移除header中的cookie
        if (StringUtils.isEmpty(sessionId)){
            fh.removeHeader(HEADER_SET_COOKIE);
            fh.removeHeader(HEADER_COOKIE);
            fh.removeHeader(HEADER_cookie);
        }else{
            String cookie = String.format("%s=%s", KEY_JSESSIONID, sessionId);
            fh.addHeader(HEADER_SET_COOKIE, cookie);
            fh.addHeader(HEADER_COOKIE, cookie);
            fh.addHeader(HEADER_cookie, cookie);
        }

        return fh;
    }


    public static void postDefault(String url, AjaxParams params,
                            AjaxCallBack<? extends Object> callBack){
        AfinalFactory.getHttp().post(url, params, callBack);
    }

}
