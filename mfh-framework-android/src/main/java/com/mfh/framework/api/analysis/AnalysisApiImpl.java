package com.mfh.framework.api.analysis;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.util.Date;

/**
 * 日结/清分/交接班
 * Created by bingshanguxue on 8/4/16.
 */
public class AnalysisApiImpl extends AnalysisApi {
    /**
     * 启动交接班统计<br>
     *
     * @param shiftId   班次（POS机本地记录并累加）
     * @param startTime 交接班开始时间（默认使用上一次交接班/日结时间）
     * @param endTime   交接班结束时间 (当前系统时间)
     */
    public static void autoShiftAnalysic(int shiftId, String startTime, String endTime,
                                         AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("shiftId", String.valueOf(shiftId));
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BIZSERVIE_AUTOSHIFITANALYSIC, params, responseCallback);
    }

    /**
     * 流水分析,查询交接班业务类型统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAggShift(int shiftId, String aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("shiftId", String.valueOf(shiftId));
        params.put("aggDate", aggDate);
        params.put("createdBy", String.valueOf(MfhLoginService.get().getHumanId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSIS_AGGSHIFT, params, responseCallback);
    }

    /**
     * 经营分析,查询交接班支付方式统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void accAnalysisAggShift(int shiftId, String aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("shiftId", String.valueOf(shiftId));
        params.put("aggDate", aggDate);
        params.put("createdBy", String.valueOf(MfhLoginService.get().getHumanId()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ACCANALYSIS_AGGSHIFT, params, responseCallback);
    }

    /**
     * 启动日结统计
     */
    public static void autoDateEnd(Date date, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("date", TimeUtil.format(date, TimeUtil.FORMAT_YYYYMMDD));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_BIZSERVIE_AUTODATEEND, params, responseCallback);
    }

    /**
     * 流水分析,查询日结支付方式统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAccDateList(Date aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("aggDate", aggDate != null ? TimeCursor.FORMAT_YYYYMMDD.format(aggDate) : "");
//        params.put("createdBy", MfhLoginService.get().getCurrentGuId());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ANALYSIS_ACCDATE_LIST, params, responseCallback);
    }

    /**
     * 经营分析,查询日结业务类型统计
     *
     * @param aggDate 起始时间，格式：YYYY-MM-DD
     */
    public static void analysisAggDateList(Date aggDate, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("wrapper", "true");
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("aggDate", aggDate != null ? TimeCursor.FORMAT_YYYYMMDD.format(aggDate) : "");
//        params.put("createdBy", MfhLoginService.get().getCurrentGuId());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_ACCANALYSIS_AGGDATE_LIST, params, responseCallback);
    }


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
