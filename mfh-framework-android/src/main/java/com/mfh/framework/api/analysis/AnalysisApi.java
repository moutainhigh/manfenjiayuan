package com.mfh.framework.api.analysis;

import com.mfh.framework.api.MfhApi;

/**
 * 日结/清分/交接班
 * Created by bingshanguxue on 8/4/16.
 */
public class AnalysisApi {
    public static String URL_ANALYSIS_ACCDATE = MfhApi.URL_BASE_SERVER + "/analysisAccDate/";

    public static String URL_BIZSERVER = MfhApi.URL_BASE_SERVER + "/bizServer/";

    /**
     * 日结经营分析查询
     * 经营分析：/analysisAggDate/list?aggDate=2016-02-17&officeId=131295
     */
    public static String URL_ACCANALYSIS_AGGDATE_LIST = MfhApi.URL_BASE_SERVER + "/analysisAggDate/list";

    /**
     * 交接班流水分析查询
     * /analysisAggShift/list?aggDate=2016-02-02&shiftId=1&createdBy=134475，finishDay参数改成aggDate
     */
    public static String URL_ANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/analysisAggShift/list";

    /**
     * 交接班经营分析查询
     * /accAnalysisAggShift/list?aggDate=2016-02-02&createdBy=134475&shiftId=1
     */
    public static String URL_ACCANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/accAnalysisAggShift/list";


    /**
     * 启动交接班统计
     * /bizServer/autoShiftAnalysis?shiftId=1&startTime=2016-02-02 07:00:00&endTime=2016-02-02 19:00:00
     */
    public static String URL_BIZSERVIE_AUTOSHIFITANALYSIC = URL_BIZSERVER + "autoShiftAnalysis";

    /**
     * 启动日结统计
     * /bizServer/autoDateEnd?date=2016-02-02
     */
    public static String URL_BIZSERVIE_AUTODATEEND = URL_BIZSERVER + "autoDateEnd";


    /**
     * 日结流水分析查询
     * 流水分析：/analysisAccDate/list?officeId=132079&aggDate=2016-02-18&wrapper=true
     */
    public static String URL_ANALYSIS_ACCDATE_LIST = URL_ANALYSIS_ACCDATE + "list";

    /**
     * 获取最后日结日期
     * /analysisAccDate/getLastAggDate?officeId=132079
     */
    public static String URL_ANALYSISACCDATE_GETLASTAGGDATE = URL_ANALYSIS_ACCDATE + "getLastAggDate";


    /**
     * 针对当前用户所属网点判断是否存在过清分时余额不足情况
     * /analysisAccDate/haveNoMoneyEnd?date=2016-02-02
     *
     * @param request date可空,默认是昨天。代表昨天包括昨天以前的时间内有无存在余额不足情况。
     */
    public static String URL_ANALYSISACCDATE_HAVENOMONEYEND = URL_ANALYSIS_ACCDATE + "haveNoMoneyEnd";

    /**
     * 检测昨日是否清分完毕：针对当前用户所属网点判断是否进行过日结清分操作；如果发现未清分，则锁定pos机，
     * 但允许调用commintCashAndTrigDateEnd接口提交营业现金，后台马上会触发一次清分。
     * /analysisAccDate/haveDateEnd?date=2016-02-02
     */
    public static String URL_ANALYSISACCDATE_HAVEDATEEND = URL_ANALYSIS_ACCDATE + "haveDateEnd";


    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     * /analysisAccDate/commintCashAndTrigDateEnd?date=2016-02-18&outTradeNo=7_1000228_1452600164232
     */
    public static String URL_ANALYSISACCDATE_COMMITCASH_TRIGDATEEND = URL_ANALYSIS_ACCDATE +
            "commintCashAndTrigDateEnd";

    /**
     * 针对当前用户所属网点提交营业现金
     * /analysisAccDate/commintCashAndTrigDateEnd?outTradeNo=7_1000228_1452600164232
     */
    public static String URL_ANALYSISACCDATE_COMMITCASH = URL_ANALYSIS_ACCDATE + "commintCash";


    public static void register() {
        URL_ANALYSIS_ACCDATE = MfhApi.URL_BASE_SERVER + "/analysisAccDate/";
        URL_BIZSERVER = MfhApi.URL_BASE_SERVER + "/bizServer/";
        URL_ACCANALYSIS_AGGDATE_LIST = MfhApi.URL_BASE_SERVER + "/analysisAggDate/list";
        URL_ANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/analysisAggShift/list";
        URL_ACCANALYSIS_AGGSHIFT = MfhApi.URL_BASE_SERVER + "/accAnalysisAggShift/list";

        URL_BIZSERVIE_AUTOSHIFITANALYSIC = URL_BIZSERVER + "autoShiftAnalysis";
        URL_BIZSERVIE_AUTODATEEND = URL_BIZSERVER + "autoDateEnd";
        URL_ANALYSIS_ACCDATE_LIST = URL_ANALYSIS_ACCDATE + "list";
        URL_ANALYSISACCDATE_GETLASTAGGDATE = URL_ANALYSIS_ACCDATE + "getLastAggDate";
        URL_ANALYSISACCDATE_HAVENOMONEYEND = URL_ANALYSIS_ACCDATE + "haveNoMoneyEnd";
        URL_ANALYSISACCDATE_HAVEDATEEND = URL_ANALYSIS_ACCDATE + "haveDateEnd";
        URL_ANALYSISACCDATE_COMMITCASH_TRIGDATEEND = URL_ANALYSIS_ACCDATE + "commintCashAndTrigDateEnd";
        URL_ANALYSISACCDATE_COMMITCASH = URL_ANALYSIS_ACCDATE + "commintCash";
    }
}
