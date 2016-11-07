package com.mfh.framework.api.invCompany;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 批发商租户
 * Created by bingshanguxue on 7/27/16.
 */
public class InvCompanyApi {
    //上线
    static final Integer STATUS_ONLINE = 2;

    public static String URL_INV_COMPANY = MfhApi.URL_BASE_SERVER + "/invCompany/";

    /**
     * 查询批发商租户，使用这个接口/invCompany/list?status=2&shortCode=，shortCode是速记码，status=2代表上线的
     * */
    public static String URL_LIST = URL_INV_COMPANY + "list";

    /**
    * 查询批发商租户
     * @param status 状态: {@link #STATUS_ONLINE 上线}
     * @param shortCode 速记码
     * @param pageInfo
     * @param responseCallback
    */
    public static void list(Integer status, String shortCode, PageInfo pageInfo,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("status", String.valueOf(status));
        if (!StringUtils.isEmpty(shortCode)){
            params.put("shortCode", shortCode);
        }

        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_LIST, params, responseCallback);
    }
}
