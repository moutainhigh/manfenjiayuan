package com.mfh.framework.api.scChainGoodsSku;

import com.mfh.framework.api.MfhApi;

/**
 * 商品Api
 * Created by bingshanguxue on 4/22/16.<br>
 * /scGoodsSku
 */
public class ScChainGoodsSkuApi {
    private final static String URL_SCCHAINGOODSSKU = MfhApi.URL_BASE_SERVER + "/scChainGoodsSku/";


    /**
     * /scChainGoodsSku/findTenantSku
     * 查询供应链商品,解决不在采购计划内的商品也能被收货<BR>
     * */
    public final static String URL_SCCHAINGOODSSKU_FIND_TENANTSKU = URL_SCCHAINGOODSSKU + "findTenantSku";

    /**
     *  /scChainGoodsSku/getTenantSkuMust?barcode=&tenantId=134651
     * 根据条码查找商品档案，没有则从产品中心查找<BR>
     * {@link #URL_SCCHAINGOODSSKU_GETTENANTSKUMUST}
     * */
    public final static String URL_SCCHAINGOODSSKU_GETTENANTSKUMUST = URL_SCCHAINGOODSSKU + "getTenantSkuMust";


    /**
     * 查询供应链商品
     * /scChainGoodsSku/findPublicChainGoodsSku?
     * */
    public final static String URL_FIND_PUBLICCHAINGOODSSKU = URL_SCCHAINGOODSSKU + "findPublicChainGoodsSku";

    /**
     * 查询一个产品sku有哪些批发商供应
     * /scChainGoodsSku/findSupplyChainGoodsSku?barcode=|proSkuId=|nameLike=
     *
     * 注意，这个接口和上面的"findPublicChainGoodsSku"接口返回的结果是一样的。
     * findPublicChainGoodsSku历史原因，里面包含了很多附加逻辑，譬如自动根据当前登录用户
     * 附加了一些条件，使用起来不方便
     */
    public final static String URL_FIND_SUPPLYCHAINGOODSSKU = URL_SCCHAINGOODSSKU + "findSupplyChainGoodsSku";


    /**
     * 批量查询批发商商品
     * */
    public final static String URL_SCCHAINGOODSSKU_LIST = URL_SCCHAINGOODSSKU + "list";


    /**
     * 查询批发商供应商商品
     * */
    public final static String URL_INVSKUPROVIDER_LIST = MfhApi.URL_BASE_SERVER + "/invSkuProvider/list";


}
