package com.mfh.enjoycity.ui.web;

import android.content.Context;

import com.mfh.framework.hybrid.WebViewUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 16/2/25.
 */
public class HybridHelper {
    /**
     * 同步Cookie
     * */
    public static void syncCookies(Context context, String url){
        String sessionId = MfhLoginService.get().getCurrentSessionId();
        if(sessionId != null){
            StringBuilder sbCookie = new StringBuilder();
            sbCookie.append(String.format("JSESSIONID=%s", sessionId));
            sbCookie.append(String.format(";domain=%s", H5Api.DOMAIN));
            sbCookie.append(String.format(";path=%s", "/"));
            String cookieValue = sbCookie.toString();

            WebViewUtils.syncCookies(context, url, cookieValue);
        }
    }
}
