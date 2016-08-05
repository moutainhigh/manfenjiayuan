package com.mfh.framework.api.analysis;

import com.mfh.framework.api.MfhApi;

/**
 * 日结/清分/交接班
 * Created by bingshanguxue on 8/4/16.
 */
public class AnalysisApi {
    public final static String URL_ANALYSIS_ACCDATE = MfhApi.URL_BASE_SERVER + "/analysisAccDate/";


    /**
     * 启动交接班统计
     * /bizServer/autoShiftAnalysis?shiftId=1&startTime=2016-02-02 07:00:00&endTime=2016-02-02 19:00:00
     */
    public final static String URL_BIZSERVIE_AUTOSHIFITANALYSIC = MfhApi.URL_BASE_SERVER + "/bizServer/autoShiftAnalysis";

    /**
     * 交接班流水分析查询
     * /analysisAggShift/list?aggDate=2016-02-02&shiftId=1&createdBy=134475，finishDay参数改成aggDate
     */
    public final static String URL_ANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/analysisAggShift/list";

    /**
     * 交接班经营分析查询
     * /accAnalysisAggShift/list?aggDate=2016-02-02&createdBy=134475&shiftId=1
     */
    public final static String URL_ACCANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/accAnalysisAggShift/list";

    /**
     * 启动日结统计
     * /bizServer/autoDateEnd?date=2016-02-02
     */
    public final static String URL_BIZSERVIE_AUTODATEEND = MfhApi.URL_BASE_SERVER + "/bizServer/autoDateEnd";

    /**
     * 日结流水分析查询
     * 流水分析：/analysisAccDate/list?officeId=132079&aggDate=2016-02-18&wrapper=true
     */
    public final static String URL_ANALYSIS_ACCDATE_LIST = URL_ANALYSIS_ACCDATE + "list";

    /**
     * 日结经营分析查询
     * 经营分析：/analysisAggDate/list?aggDate=2016-02-17&officeId=131295
     */
    public final static String URL_ACCANALYSIS_AGGDATE_LIST = MfhApi.URL_BASE_SERVER + "/analysisAggDate/list";

    /**
     * 获取最后日结日期
     * /analysisAccDate/getLastAggDate?officeId=132079
     */
    public final static String URL_ANALYSISACCDATE_GETLASTAGGDATE = URL_ANALYSIS_ACCDATE + "getLastAggDate";


    /**
     * 针对当前用户所属网点判断是否进行过日结操作
     * /analysisAccDate/haveDateEnd?date=2016-02-02
     */
    public final static String URL_ANALYSISACCDATE_HAVEDATEEND = URL_ANALYSIS_ACCDATE + "haveDateEnd";

    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     * /analysisAccDate/commintCashAndTrigDateEnd?date=2016-02-18&outTradeNo=7_1000228_1452600164232
     */
    public final static String URL_ANALYSISACCDATE_COMMITCASH_TRIGDATEEND = URL_ANALYSIS_ACCDATE + "commintCashAndTrigDateEnd";
}
