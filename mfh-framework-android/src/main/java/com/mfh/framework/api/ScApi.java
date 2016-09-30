package com.mfh.framework.api;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 商超
 * Created by bingshanguxue on 4/19/16.
 */
public class ScApi {


    /**摇一摇·店铺*/
    public final static String URL_WX_SHOP_DEVICE_PAGE = MfhApi.URL_BASE_SERVER + "/wxShopDevicePage/list";

    /**
     * 查询商品销量记录
     * /productAggDate/list?officeId=135852&proSkuId=38968
     * officeId代表当前登录网点， proSkuId是产品sku编号
     * */
    public final static String URL_PRODUCT_AGGDATE_LIST = MfhApi.URL_BASE_SERVER + "/productAggDate/list";




    /**
     * 查询商品销量记录
     * @param proSkuId 产品sku编号
     * */
    public static void productAggDateList(Long proSkuId, PageInfo pageInfo,
                                  AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        params.put("proSkuId", String.valueOf(proSkuId));

        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }

        AfinalFactory.postDefault(URL_PRODUCT_AGGDATE_LIST, params, responseCallback);
    }
}
