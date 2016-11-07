package com.mfh.framework.api.invCompProvider;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 批发商的私有供应商
 * Created by bingshanguxue on 7/27/16.
 */
public class InvComProviderApi {

    public static String URL_INV_COMPROVIDER = MfhApi.URL_BASE_SERVER + "/invCompProvider/";

    /**
     * 查询批发商的私有供应商
     * */
    public static String URL_FIND_MYPROVIDERS = URL_INV_COMPROVIDER + "findMyProviders";

    /**
    * 查询批发商租户
     * @param status 状态: {@link #STATUS_ONLINE 上线}
     * @param shortCode 速记码
     * @param pageInfo
     * @param responseCallback
    */
    public static void findMyProviders(PageInfo pageInfo,
                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_MYPROVIDERS, params, responseCallback);
    }
}
