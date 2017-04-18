package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 5/20/16.
 */
public class ScGoodsSkuApiImpl extends ScGoodsSkuApi {
    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     */
    public static void getLocalByBarcode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETLOCAL_BYBARCODE, params, responseCallback);
    }

    /**
     * 查询前台类目商品
     * @param categoryId 前台类目编号
     * */
    public static void findGoodsListByFrontCategory(Long categoryId, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (categoryId != null) {
            params.put("categoryId", String.valueOf(categoryId));
        }
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 查询后台类目商品
     * @param procateId 后台类目编号
     * */
    public static void findGoodsListByBackendCategory(Long procateId, PageInfo pageInfo,
                                                    AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (procateId != null) {
            params.put("procateId", String.valueOf(procateId));
        }
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 查询商品
     * @param barcode 商品条码
     * */
    public static void findGoodsListByBarcode(String barcode, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)) {
            params.put("barcode", barcode);
        }
        params.put("needSellNum", "true");//销量
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }

    /**
     * 根据商品名称查询商品，模糊查询
     * */
    public static void findGoodsListByName(String name, PageInfo pageInfo,
                                     AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(name)) {
            params.put("name", name);
        }
        params.put("needSellNum", "true");
        params.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDGOODSLIST, params, responseCallback);
    }


    /**
     * 查询租户的商品档案
     * <ol>
     * 适用场景
     * <li>门店商品报损</li>
     * <li>门店发送商品</li>
     * <li>PDA盘点商品</li>
     * <li>PDA商品绑定货架</li>
     * </ol>
     */
    public static void getGoodsByBarCode(String barcode,
                                                   AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GETGOODS_BYBARCODE, params, responseCallback);
    }


    /**
     * 批量查询网点商品信息
     * @param netId 店铺编号
     * @param proSkuIds 商品编号
     * */
    public static void findOnlineGoodsList(Long netId, String proSkuIds, PageInfo pageInfo,
                                           AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(proSkuIds)) {
            params.put("proSkuIds", proSkuIds);
        }
        params.put("netId", String.valueOf(netId));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDONLINEGOODSLIST, params, responseCallback);
    }
    /**
     * 批量查询网点商品信息
     * @param netId 店铺编号
     * @param frontCategoryId 类目编号
     * */
    public static void findOnlineGoodsList2(Long netId, Long frontCategoryId, PageInfo pageInfo,
                                           AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("netId", String.valueOf(netId));
        params.put("frontCategoryId", String.valueOf(frontCategoryId));
        if (pageInfo != null){
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FINDONLINEGOODSLIST, params, responseCallback);
    }
}
