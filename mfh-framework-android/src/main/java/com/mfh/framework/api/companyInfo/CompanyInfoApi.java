package com.mfh.framework.api.companyInfo;

import com.mfh.framework.api.MfhApi;

/**
 * 租户
 * Created by bingshanguxue on 7/27/16.
 */
public class CompanyInfoApi {

    public static String URL_COMPANYINFO = MfhApi.URL_BASE_SERVER + "/companyInfo/";

    /**
     * 查询门店
     */
    public static String URL_FIND_PUBLICCOMPANYINFO = URL_COMPANYINFO + "findPublicCompanyInfo";

    /**
     * 查询网点信息
     */
    public static String URL_GETNETINFO_BYID = URL_COMPANYINFO + "getNetInfoById";

    public static String URL_COMPANYINFO_GETBYID = URL_COMPANYINFO + "getById";

    /**
     * 查询快递公司：/companyInfo/comnQuery?kind=code&viewId=2，快递公司
     */
    public static String URL_COMPANYINFO_COMNQUERY = URL_COMPANYINFO + "comnQuery";

    public static void register() {
        URL_COMPANYINFO = MfhApi.URL_BASE_SERVER + "/companyInfo/";
        URL_FIND_PUBLICCOMPANYINFO = URL_COMPANYINFO + "findPublicCompanyInfo";
        URL_GETNETINFO_BYID = URL_COMPANYINFO + "getNetInfoById";
        URL_COMPANYINFO_GETBYID = URL_COMPANYINFO + "getById";
        URL_COMPANYINFO_COMNQUERY = URL_COMPANYINFO + "comnQuery";
    }

}
