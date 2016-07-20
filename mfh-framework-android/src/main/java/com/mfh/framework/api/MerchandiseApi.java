package com.mfh.framework.api;

/**
 * 商品Api
 * Created by bingshanguxue on 4/22/16.<br>
 * /scGoodsSku
 */
public class MerchandiseApi {
    private final static String URL_SCCHAINGOODSSKU = MfhApi.URL_BASE_SERVER + "/scChainGoodsSku/";


    /**
     * /scChainGoodsSku/findTenantSku
     * 查询供应链商品,解决不在采购计划内的商品也能被收货<BR>
     * {@link #URL_CHAINGOODSSKU_FIND_PUBLICCHAINGOODSSKU}
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
     * */
    public final static String URL_CHAINGOODSSKU_FIND_PUBLICCHAINGOODSSKU = URL_SCCHAINGOODSSKU + "findPublicChainGoodsSku";

    /**
     * 批量查询批发商商品
     * */
    public final static String URL_SCCHAINGOODSSKU_LIST = URL_SCCHAINGOODSSKU + "list";


    /**
     * 查询批发商供应商商品
     * */
    public final static String URL_INVSKUPROVIDER_LIST = MfhApi.URL_BASE_SERVER + "/invSkuProvider/list";


}
