package com.mfh.framework.api.companyInfo;

import com.mfh.framework.api.MfhApi;

/**
 * 租户
 * Created by bingshanguxue on 7/27/16.
 */
public class CompanyInfoApi {

    public final static String URL_COMPANYINFO = MfhApi.URL_BASE_SERVER + "/companyInfo/";

    /**
     * 查询门店
     * */
    public final static String URL_FIND_PUBLICCOMPANYINFO = URL_COMPANYINFO + "findPublicCompanyInfo";

    /**
     * 查询网点信息
     * */
    public final static String URL_GETNETINFO_BYID = URL_COMPANYINFO + "getNetInfoById";
    /**
     * 根据经纬度查询网点
     * */
    public final static String URL_FIND_SERVICEDNETS_FORUSERPOS = URL_COMPANYINFO + "findServicedNetsForUserPos";

}
