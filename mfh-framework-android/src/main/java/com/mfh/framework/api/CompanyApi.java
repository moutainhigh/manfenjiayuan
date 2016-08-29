package com.mfh.framework.api;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 7/12/16.
 */
public class CompanyApi {
    public static final String URL_COMPANYINFO_GETBYID = MfhApi.URL_BASE_SERVER + "/companyInfo/getById";

    /**查询快递公司：/companyInfo/comnQuery?kind=code&viewId=2，快递公司*/
    public static final String URL_COMPANYINFO_COMNQUERY = MfhApi.URL_BASE_SERVER + "/companyInfo/comnQuery";

    /**
     * 查询公司
     */
    public static void comnQuery(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("kind", "code");
        params.put("viewId", "2");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_COMPANYINFO_COMNQUERY, params, responseCallback);
    }

    /**
     * 查询公司
     * @param companyId
     */
    public static void getById(Long companyId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(companyId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_COMPANYINFO_GETBYID, params, responseCallback);
    }

}
