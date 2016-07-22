package com.mfh.framework.api.scChainGoodsSku;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * Created by bingshanguxue on 4/26/16.
 */
public class ScChainGoodsSkuApiImpl extends ScChainGoodsSkuApi {


    /**
     * 查询供应链商品,解决不在采购计划内的商品也能被收货
     * <p/>
     * <ol>
     * 适用场景
     * <li>门店采购收货，companyId字段可查询指定批发商的商品</li>
     * <li>批发商拣货（发货），companyId不需要填</li>
     * </ol>
     *
     * @param barcode   商品条码
     * @param companyId 批发商编号(可空，空值就取当前登录用户)
     */
    public static void findTenantSku(String barcode, Long companyId, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        if (companyId != null) {
            params.put("companyId", String.valueOf(companyId));
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_SCCHAINGOODSSKU_FIND_TENANTSKU, params, responseCallback);
    }

    /**
     * 查询供应链商品,解决不在采购计划内的商品也能被收货
     * <p/>
     * <ol>
     * 适用场景
     * <li>门店采购收货，companyId字段可查询指定批发商的商品</li>
     * <li>批发商拣货（发货），companyId不需要填</li>
     * </ol>
     *
     * @param barcode   商品条码
     * @param tenantId 批发商编号(可空，空值就取当前登录用户)
     */
    public static void getTenantSkuMust(String barcode, Long tenantId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        if (tenantId != null) {
            params.put("tenantId", String.valueOf(tenantId));
        }

        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_SCCHAINGOODSSKU_GETTENANTSKUMUST, params, responseCallback);
    }

    /**
     * 查询供应链商品,解决不在采购计划内的商品也能被收货
     * <ol>
     *     适用场景
     *     <li>查询洗衣类目商品商品</li>
     * </ol>
     */
    public static void findPublicChainGoodsSku(Long frontCategoryId, Long netId, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("frontCategoryId", String.valueOf(frontCategoryId));
        if (netId != null) {
            params.put("netId", String.valueOf(netId));
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FIND_PUBLICCHAINGOODSSKU, params, responseCallback);
    }
    /**
     * 查询供应链商品
     * <ol>
     *     适用场景
     *     <li>门店商品预定</li>
     * </ol>
     */
    public static void findPublicChainGoodsSku2(Long frontCategoryId, Long companyId, String barcode,
                                                PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("frontCategoryId", String.valueOf(frontCategoryId));
        if (companyId != null) {
            params.put("companyId", String.valueOf(companyId));
        }
        if (!StringUtils.isEmpty(barcode)){
            params.put("barcode", barcode);
        }
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FIND_PUBLICCHAINGOODSSKU, params, responseCallback);
    }
    /**
     * 查询供应链商品
     * <ol>
     *     适用场景
     *     <li>门店or批发商PDA商品管理</li>
     * </ol>
     */
    public static void findSupplyChainGoodsSku(String barcode, Long proSkuId, String nameLike,
                                                AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (!StringUtils.isEmpty(barcode)){
            params.put("barcode", barcode);
        }
        if (proSkuId != null) {
            params.put("companyId", String.valueOf(proSkuId));
        }
        if (!StringUtils.isEmpty(nameLike)){
            params.put("nameLike", nameLike);
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_FIND_SUPPLYCHAINGOODSSKU, params, responseCallback);
    }
    /**
     * 批量查询供应链商品
     * @param proSkuId 格式：“16172,16534”,逗号隔开。
     * 适用场景：批发商PDA根据拣货单发货，拣货单无商品批发价，调用该接口获取批发价
     */
    public static void scChainGoodsSkuList(String proSkuId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("proSkuId", proSkuId);

        AfinalFactory.getHttp(true).post(URL_SCCHAINGOODSSKU_LIST, params, responseCallback);
    }

    /**
     * 查询批发商供应商商品
     * 适用场景：批发商收货
     */
    public static void listInvSkuProvider(Long providerId, String barcode, PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("storeType", "1");
        if (providerId != null) {
            params.put("providerId", String.valueOf(providerId));
        }
        params.put("barcode", barcode);
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_INVSKUPROVIDER_LIST, params, responseCallback);
    }

}
