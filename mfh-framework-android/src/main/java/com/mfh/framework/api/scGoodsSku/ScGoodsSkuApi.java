package com.mfh.framework.api.scGoodsSku;

import com.mfh.framework.api.MfhApi;

/**
 * 商超
 * Created by bingshanguxue on 5/20/16.
 */
public class ScGoodsSkuApi {
    public static String URL_SCGOODSSKU = MfhApi.URL_BASE_SERVER + "/scGoodsSku/";

    /**
     * 同步微超商品列表
     */
    public static String URL_DOWNLOAD_POS_PRODUCT = URL_SCGOODSSKU + "downLoadPosProduct";

    /**
     * 查询网点库存商品信息列表（包括本店商品库存、采购价、售价和商品档案基本信息等）.
     * 其中needSellNum代表是否需要返回商品30天平均销量
     * /scGoodsSku/findGoodsList?categoryId=&netId=&barcode=&name=&needSellNum=false
     */
    static String URL_FINDGOODSLIST = URL_SCGOODSSKU + "findGoodsList";
    /**
     * /scGoodsSku/getLocalByBarcode
     * 查询库存商品－－
     */
    static String URL_GETLOCAL_BYBARCODE = URL_SCGOODSSKU + "getLocalByBarcode";

    /**
     * /scGoodsSku/checkWithBuyInfoByBarcode
     */
    static String URL_CHECKWITHBUYINFO_BYBARCODE = URL_SCGOODSSKU
            + "checkWithBuyInfoByBarcode";

    /**
     * 盘点查询商品
     * <ul>
     * 适用场景：
     * <li>收银机－－根据条码查询库存商品，报损商品</li>
     * </ul>
     */
    static String URL_GETGOODS_BYBARCODE = URL_SCGOODSSKU + "getGoodsByBarCode";


    /**
     * 从批发商导入某个类目的商品到当前门店
     * 注意此操作只会同步批发商的商品库到门店的商品库，POS机的商品库同步是另外一个逻辑
     * /scGoodsSku/importFromChainSku?sendTenantId=134651&cateType=2&startCursor=2015-01-01 10:00:00
     *
     * @param sendTenantId 平台上的某个批发商
     * @param cateType 类目, 不传cateType,由后台控制，导入全部类目商品
     * @param startCursor 时间戳, 默认值 "2015-01-01 10:00:00"
     * */
    static String URL_IMPORT_FROM_CHAINSKU = URL_SCGOODSSKU + "importFromChainSku";

    /**
     * 批量查询商品信息
     */
    static String URL_FINDONLINEGOODSLIST = URL_SCGOODSSKU + "findOnlineGoodsList";

    public static void register() {
        URL_SCGOODSSKU = MfhApi.URL_BASE_SERVER + "/scGoodsSku/";
        URL_DOWNLOAD_POS_PRODUCT = URL_SCGOODSSKU + "downLoadPosProduct";
        URL_FINDGOODSLIST = URL_SCGOODSSKU + "findGoodsList";
        URL_GETLOCAL_BYBARCODE = URL_SCGOODSSKU + "getLocalByBarcode";
        URL_CHECKWITHBUYINFO_BYBARCODE = URL_SCGOODSSKU
                + "checkWithBuyInfoByBarcode";
        URL_GETGOODS_BYBARCODE = URL_SCGOODSSKU + "getGoodsByBarCode";
        URL_IMPORT_FROM_CHAINSKU = URL_SCGOODSSKU + "importFromChainSku";

        URL_FINDONLINEGOODSLIST = URL_SCGOODSSKU + "findOnlineGoodsList";
    }


}
