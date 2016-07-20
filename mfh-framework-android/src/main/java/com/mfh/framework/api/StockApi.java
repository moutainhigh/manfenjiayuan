package com.mfh.framework.api;

/**
 * 库存Api
 * Created by bingshanguxue on 4/22/16.
 */
public class StockApi {

    //库存商品
    private final static String URL_INVSKUSTORE = MfhApi.URL_BASE_SERVER + "/invSkuStore/";

    /**查找可出库的或已出库的包裹列表*/
    public static final String URL_STOCK_FIND_STOCKOUT = MfhApi.URL_BASE_SERVER + "/pmcstock/findStockOut";
    /**包裹出库操作(支持批量)*/
    public static final String URL_STOCK_OUT = MfhApi.URL_BASE_SERVER + "/pmcstock/stockOut";

    /**
     * 更新商品信息－－
     * /scGoodsSku/update?jsonStr={"id":...,"costPrice"...,"quantity":...,"lowerLimit":...., "tenantId":....},
     * 注意：这里的tenantId就是店的租户，不是CATEGORY_TENANT_ID(130222)
     * */
    public final static String URL_INVSKUSTORE_UPDATE = URL_INVSKUSTORE + "update";

    /**
     * 商品和货架绑定
     * /invSkuStore/bindRackNo?barcode=998800000000&rackNo=00102，
     */
    public final static String URL_GOODSSKU_BINDRACKNO = URL_INVSKUSTORE + "bindRackNo";

    /**
     * 当前登录网点的操作人员，通过指定一个批发商，自动生成配送单
     * /invSkuStore/autoAskSendOrder?chainCompanyId=134651
     */
    public final static String URL_INVSKUSTORE_AUTOASKSENDORDER = URL_INVSKUSTORE + "autoAskSendOrder";


    /**
     * 根据条码查询库存商品,如果库存中没有则从租户档案中自动建立库存。门店和批发都适用
     * /invSkuStore/getByBarcodeMust?barcode=998800000000
     */
    public final static String URL_INVSKUSTORE_GETBYBARCODEMUST = URL_INVSKUSTORE + "getByBarcodeMust";

    /**
     * 库存转换，其中的id都是库存sku商品id
     * /invSkuStore/changeSkuStore?
     * sendItems=[{"id":111,"quantity":1.0},
     * {"id":222,"quantity":1.0}]
     * &receiveItem={"id":333,"quantity":1.0}
     */
    public final static String URL_INVSKUSTORE_CHANGESKUSTORE = URL_INVSKUSTORE + "changeSkuStore";

}
