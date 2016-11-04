package com.mfh.framework.api.stock;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 03/11/2016.
 */

public class StockApi {

    public final static String URL_STOCK = MfhApi.URL_BASE_SERVER + "/stock/";

    /**
     * 查询快递员所属公司
     */
    public static final String URL_FIND_COMPANY_BY_HUMANID = URL_STOCK + "receiveBatch/findFdCompanyByHumanId";

    public static void findCompanyByHumanId(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_FIND_COMPANY_BY_HUMANID, params, responseCallback);
    }
}
