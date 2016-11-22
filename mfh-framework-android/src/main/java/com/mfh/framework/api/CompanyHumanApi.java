package com.mfh.framework.api;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 账号
 * Created by bingshanguxue on 8/30/16.
 */
public class CompanyHumanApi {
    public static String URL_COMPANYHUMAN = MfhApi.URL_BASE_SERVER + "/companyHuman/";

    /**
     * 查询子账号
     * /companyHuman/findCompUserPwdInfo?page=-1&rows=
     */
    public static String URL_FINDCOMPUSER_PWDINFO = URL_COMPANYHUMAN + "findCompUserPwdInfo";

    /**
     * 查询部门子账号
     * /pmc/companyHuman/listWorkerBeanInfoOfCompany?page=-1&rows=
     */
    public static String URL_LISTWORKERBEANINFO_OFCOMPANY = URL_COMPANYHUMAN + "listWorkerBeanInfoOfCompany";

    public static void register() {
        URL_COMPANYHUMAN = MfhApi.URL_BASE_SERVER + "/companyHuman/";

        URL_FINDCOMPUSER_PWDINFO = URL_COMPANYHUMAN + "findCompUserPwdInfo";
        URL_LISTWORKERBEANINFO_OFCOMPANY = URL_COMPANYHUMAN + "listWorkerBeanInfoOfCompany";
    }


    /**
     * 查询子账号
     */
    public static void findCompUserPwdInfo(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FINDCOMPUSER_PWDINFO, params, responseCallback);
    }

    /**
     * 查询部门子账号
     */
    public static void listWorkerBeanInfoOfCompany(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_LISTWORKERBEANINFO_OFCOMPANY, params, responseCallback);
    }
}
