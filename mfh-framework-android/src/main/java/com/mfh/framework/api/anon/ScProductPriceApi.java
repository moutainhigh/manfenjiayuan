package com.mfh.framework.api.anon;

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
public class ScProductPriceApi {
    public final static String URL_ANON_SC_PRODUCTPRICE = MfhApi.URL_BASE_SERVER + "/anon/sc/productPrice/";

    /**
     * /anon/sc/productPrice/findPubSkusByFrontCatalog?frontCataLogId=&priceMask=0
     * 根据前台类目查找有效的商品列表(前台类目可能又包含后台类目),返回的id就是proSkuId，另外还有productId
     * */
    public final static String URL_FINDPUBSKUS_BYFRONTCATALOG= URL_ANON_SC_PRODUCTPRICE + "findPubSkusByFrontCatalog";


    /**
     * 根据前台类目查找有效的商品列表
     * <p/>
     * <ol>
     * 适用场景
     * <li>门店导入前台类目商品查询时调用</li>
     * </ol>
     * @param frontCataLogId 前台类目编号
     * @param priceMask 从信息模型上，一个productId其实可能对应多个proSkuId，
     *                  但在你pos这边应该不存在这种情况，你可以固定写死priceMask=0
     */
    public static void findProductByFrontCatalog(Long frontCataLogId, PageInfo pageInfo,
                                                 AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (frontCataLogId != null) {
            params.put("frontCataLogId", String.valueOf(frontCataLogId));
        }
        params.put("priceMask", "0");
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FINDPUBSKUS_BYFRONTCATALOG, params, responseCallback);
    }

}
