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
     * /anon/sc/productPrice/findProductSku?barcode
     * 根据条码查询平台商品档案
     * */
    public final static String URL_FIND_PRODUCTSKU= URL_ANON_SC_PRODUCTPRICE + "findProductSku";


    /**
     * 根据前台类目查找有效的商品列表
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
        params.put("priceMask", "0");
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FINDPUBSKUS_BYFRONTCATALOG, params, responseCallback);
    }

    /**
     * 根据条码查询平台商品档案
     * @param barcode 商品条码
     */
    public static void findProductSku(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_PRODUCTSKU, params, responseCallback);
    }

}
