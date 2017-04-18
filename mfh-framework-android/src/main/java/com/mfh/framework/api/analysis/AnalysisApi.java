package com.mfh.framework.api.analysis;

import com.mfh.framework.api.MfhApi;

/**
 * 统计分析API: 日结/清分/交接班
 * Created by bingshanguxue on 8/4/16.
 */
public class AnalysisApi {
    public static String URL_ANALYSIS_ACCDATE = MfhApi.URL_BASE_SERVER + "/analysisAccDate/";

    public static String URL_BIZSERVER = MfhApi.URL_BASE_SERVER + "/bizServer/";


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
        URL_ANALYSISACCDATE_GETLASTAGGDATE = URL_ANALYSIS_ACCDATE + "getLastAggDate";
        URL_ANALYSISACCDATE_HAVENOMONEYEND = URL_ANALYSIS_ACCDATE + "haveNoMoneyEnd";
        URL_ANALYSISACCDATE_HAVEDATEEND = URL_ANALYSIS_ACCDATE + "haveDateEnd";
        URL_ANALYSISACCDATE_COMMITCASH_TRIGDATEEND = URL_ANALYSIS_ACCDATE + "commintCashAndTrigDateEnd";
        URL_ANALYSISACCDATE_COMMITCASH = URL_ANALYSIS_ACCDATE + "commintCash";
    }
}
