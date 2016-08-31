package com.mfh.framework.api.scProduct;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScProductApi {
    public final static String URL_ANON_SC_PRODUCT = MfhApi.URL_BASE_SERVER + "/anon/sc/product/";

    /**
     * /anon/sc/product/findProductByFrontCatalog?frontCataLogId=
     * */
    public final static String URL_FINDPRODUCT_BYFRONTCATALOG= URL_ANON_SC_PRODUCT + "findProductByFrontCatalog";

    /**
     * 查询前台类目商品商品
     * <p/>
     * <ol>
     * 适用场景
     * <li>门店导入前台类目商品查询时调用</li>
     * </ol>
     * @param frontCataLogId 前台类目编号
     */
    public static void findProductByFrontCatalog(Long frontCataLogId, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (frontCataLogId != null) {
            params.put("frontCataLogId", String.valueOf(frontCataLogId));
        }
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FINDPRODUCT_BYFRONTCATALOG, params, responseCallback);
    }
}
