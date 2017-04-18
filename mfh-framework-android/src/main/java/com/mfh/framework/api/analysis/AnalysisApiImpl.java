package com.mfh.framework.api.analysis;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 日结/清分/交接班
 * Created by bingshanguxue on 8/4/16.
 */
public class AnalysisApiImpl extends AnalysisApi {

    /**
     * 针对当前用户所属网点判断是否存在过清分时余额不足情况
     */
    public static void haveNoMoneyEnd(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_HAVENOMONEYEND, params, responseCallback);
    }

    /**
     * 提交营业现金，并触发一次日结操作
     */
    public static void commintCashAndTrigDateEnd(String outTradeNo,
                                                 AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("outTradeNo", outTradeNo);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_COMMITCASH_TRIGDATEEND, params, responseCallback);
    }

    /**
     * 提交营业现金
     */
    public static void commintCash(String outTradeNo,
                                                 AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("outTradeNo", outTradeNo);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSISACCDATE_COMMITCASH, params, responseCallback);
    }
}
