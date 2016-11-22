package com.mfh.framework.api.stock;

import com.mfh.framework.api.MfhApi;

/**
 * Created by bingshanguxue on 03/11/2016.
 */

public class StockApi {

    public static String URL_STOCK = MfhApi.URL_BASE_SERVER + "/stock/";

    /**
     * 查询快递员所属公司
     */
    static String URL_RECEIVEBATCH_FINDCOMPANY_BYHUMANID = URL_STOCK + "receiveBatch/findFdCompanyByHumanId";
    /**
     * 代收快递创建批次
     */
    static String URL_RECEIVEBATCH_CREATEANDFEE = URL_STOCK + "receiveBatch/createAndFee";
    /**
     * 添加快递身份 /stock/receiveBatch/saveHumanFdCompany?humanId=0&companyId=0，
     */
    static String URL_RECEIVEBATCH_SAVEHUMANFDCOMPANY = URL_STOCK + "receiveBatch/saveHumanFdCompany";
    /**
     * 查询快递
     */
    public static String URL_RECEIVEBATCH_COMNQUERY = URL_STOCK + "receiveBatch/comnQuery";
    /**
     * 快递入库，查询用户
     */
    static String URL_RECEIVEORDER_FINDHUMANINFO_BYMOBILE = URL_STOCK + "receiveOrder/findHumanInfoByMobile";
    /**
     * 在批次中录入具体快递明细
     */
    static String URL_RECEIVEORDER_STOCKINITEMS = URL_STOCK + "receiveOrder/stockInItems";

    public static void register() {
        URL_STOCK = MfhApi.URL_BASE_SERVER + "/stock/";
        URL_RECEIVEBATCH_FINDCOMPANY_BYHUMANID = URL_STOCK + "receiveBatch/findFdCompanyByHumanId";
        URL_RECEIVEBATCH_CREATEANDFEE = URL_STOCK + "receiveBatch/createAndFee";
        URL_RECEIVEBATCH_SAVEHUMANFDCOMPANY = URL_STOCK + "receiveBatch/saveHumanFdCompany";
        URL_RECEIVEBATCH_COMNQUERY = URL_STOCK + "receiveBatch/comnQuery";
        URL_RECEIVEORDER_FINDHUMANINFO_BYMOBILE = URL_STOCK + "receiveOrder/findHumanInfoByMobile";
        URL_RECEIVEORDER_STOCKINITEMS = URL_STOCK + "receiveOrder/stockInItems";
    }


}
