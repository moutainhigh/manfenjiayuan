package com.mfh.framework.api;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 8/16/16.
 */
public class ProductCatalogApi {
    public final static String URL_PRODUCT_CATALOG = MfhApi.URL_BASE_SERVER + "/anon/sc/productCatalog/";

    /**
     * /anon/sc/productCatalog/delete?id=  删除关系接口
     * */
    public final static String URL_DELETE = URL_PRODUCT_CATALOG + "delete";
    /**
     * 同步前台类目商品关系列表
     *  /anon/sc/productCatalog/downLoadProductCatalog?tenantId=134342&startCursor=2015-01-01 10:00:00&page=1&rows=20
     * */
    public final static String URL_DOWNLOAD_PRODUCTCATALOG = URL_PRODUCT_CATALOG + "downLoadProductCatalog";

    /**
     * 把几个商品添加到指定前台类目中：  /anon/sc/productCatalog/addToCatalog?groupIds=3397&productIds=20551&catalogType=1
     其中groupIds为建好的前台类目，productIds为商品的spuId（不是skuId）
     spuId就是productId
     */
    public final static String URL_ADD2CATEGORY = URL_PRODUCT_CATALOG + "addToCatalog";




    /**
     * 删除前台类目和商品关系表
     * */
    public static void delete(Long id, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_DELETE, params, responseCallback);
    }

    /**
     * 同步前台类目和商品关系表
     * */
    public static void downLoadProductCatalog(String startCursor, PageInfo pageInfo,
                                              AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (StringUtils.isEmpty(startCursor)){
            params.put("groupIds", startCursor);
        }

        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_DOWNLOAD_PRODUCTCATALOG, params, responseCallback);
    }

    /**
     * 把几个商品添加到指定前台类目中：
     * @param groupIds 前台类目
     * @param productIds 商品的spuId（不是skuId）
     * */
    public static void add2Category(String groupIds, String productIds,
                                            AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("groupIds", groupIds);
        params.put("productIds", productIds);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_ADD2CATEGORY, params, responseCallback);
    }

}
