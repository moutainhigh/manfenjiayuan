package com.mfh.framework.api.scGoodsSku;

import com.mfh.framework.api.MfhApi;

/**
 * 商超
 * Created by bingshanguxue on 5/20/16.
 */
public class ScGoodsSkuApi {
    private final static String URL_SCGOODSSKU = MfhApi.URL_BASE_SERVER + "/scGoodsSku/";

    /**
     * 同步微超商品列表
     */
    public static final String URL_DOWNLOAD_POS_PRODUCT = URL_SCGOODSSKU + "downLoadPosProduct";
    /**
     * 指定网点可同步sku总数查询接口： /scGoodsSku/countNetSyncAbleSkuNum?netId=132079
     */
    public final static String URL_SCGOODSSKU_COUNTNETSYNCABLESKUNUM = URL_SCGOODSSKU + "countNetSyncAbleSkuNum";

    /**
     * 查询网点库存商品信息列表（包括本店商品库存、采购价、售价和商品档案基本信息等）.
     * 其中needSellNum代表是否需要返回商品30天平均销量
     * /scGoodsSku/findGoodsList?categoryId=&netId=&barcode=&name=&needSellNum=false
     */
    public final static String URL_FINDGOODSLIST = URL_SCGOODSSKU + "findGoodsList";
    /**
     * /scGoodsSku/getLocalByBarcode
     * 查询库存商品－－
     */
    public final static String URL_GETLOCAL_BYBARCODE = URL_SCGOODSSKU + "getLocalByBarcode";
    /**
     * 查询发布商品/scGoodsSku/getByBarcode?barcode=77777777&&JSESSIONID=7a6b9fe4-f6fb-4985-9810-6a7c544eeb0d
     * 根据条码逐级查找商品：
     * 若门店中存在该商品则返回信息中id、tenantSkuId、proSkuId、productId都不为空，且quantity和costPrice都有值
     * 若仅在租户存在则返回信息中tenantSkuId、proSkuId、productId不为空，且costPrice有值， quantity为0;
     * 若仅在产品中心中存在则返回信息中proSkuId、productId不为空, costPrice为空，quantity为0；
     * 若产品中心也不存在，则返回null
     */
    public final static String URL_CHECKWITHBUYINFO_BYBARCODE = URL_SCGOODSSKU
            + "checkWithBuyInfoByBarcode";

    /**
     * 查询批发商采购商品
     * 适用场景：门店采购查询商品
     */
    public final static String URL_SCGOODSKU_FIND_STOREWITHCHAINSKU = URL_SCGOODSSKU + "findStoreWithChainSku";

    /**
     * 店家商品建档入库
     */
    public final static String URL_SCGOODSSKU_STOREIN = URL_SCGOODSSKU + "storeIn";


    /**
     * 盘点查询商品
     * <ul>
     * 适用场景：
     * <li>收银机－－根据条码查询库存商品，报损商品</li>
     * </ul>
     */
    public final static String URL_GETGOODS_BYBARCODE = URL_SCGOODSSKU + "getGoodsByBarCode";

    /**
     * 查询库存商品:库存成本，批次流水，库存调拨－－
     * <p/>
     * <ul>
     * 适用场景：
     * <li>手持终端－－根据条码查询库存商品，修改商品零售价和安全库存</li>
     * </ul>
     */
    public final static String URL_SCGOODSSKU_LIST = URL_SCGOODSSKU + "list";

    /**
     * 查询发布商品/scGoodsSku/getByBarcode?barcode=77777777&&JSESSIONID=7a6b9fe4-f6fb-4985-9810-6a7c544eeb0d
     * 根据条码逐级查找商品：
     * 若门店中存在该商品则返回信息中id、tenantSkuId、proSkuId、productId都不为空，且quantity和costPrice都有值
     * 若仅在租户存在则返回信息中tenantSkuId、proSkuId、productId不为空，且costPrice有值， quantity为0;
     * 若仅在产品中心中存在则返回信息中proSkuId、productId不为空, costPrice为空，quantity为0；
     * 若产品中心也不存在，则返回null
     */
    public final static String URL_GET_BYBARCODE = URL_SCGOODSSKU + "getByBarcode";

    /**
     * 从批发商导入某个类目的商品到当前门店,
     * 其中sendTenantId是平台上的某个批发商，目前只能写死是米西配送。
     * startCursor是游标，同步一次后pos机本地记录一下后台返回的最大日期值，下次从这个值作为参数传递
     * /scGoodsSku/importFromChainSku?sendTenantId=134651&cateType=2&startCursor=2015-01-01 10:00:00
     * */
    public final static String URL_IMPORT_FROM_CHAINSKU = URL_SCGOODSSKU + "importFromChainSku";

}
