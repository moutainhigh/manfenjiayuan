package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.api.MfhApi;

/**
 * 库存商品
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreApi {

    private final static String URL_INVSKUSTORE = MfhApi.URL_BASE_SERVER + "/invSkuStore/";

    /**
     * 更新商品信息－－
     * /invSkuStore/update?jsonStr={"id":...,"costPrice"...,"quantity":...,"lowerLimit":...., "tenantId":....},
     * 注意：这里的tenantId就是店的租户，不是CATEGORY_TENANT_ID(130222)
     * */
    public final static String URL_UPDATE = URL_INVSKUSTORE + "update";

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


    /**
     * 当前登录人员，把平台商品导入到本店仓储中
     * /invSkuStore/importFromCenterSkus?proSkuIds=111,222
     */
    public final static String URL_IMPORT_FROMCENTERSKUS = URL_INVSKUSTORE + "importFromCenterSkus";

}
