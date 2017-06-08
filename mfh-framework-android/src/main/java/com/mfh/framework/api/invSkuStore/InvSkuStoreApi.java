package com.mfh.framework.api.invSkuStore;

import com.mfh.framework.api.MfhApi;

/**
 * 库存商品
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreApi {

    public static String URL_INVSKUSTORE = MfhApi.URL_BASE_SERVER + "/invSkuStore/";


    /**
     * 当前登录用户更改其本店库存商品的线上售卖状态,0代表下线，1代表上线
     * /invSkuStore/updateStatus?status=0|1&id=11111
     * 或/invSkuStore/updateStatus?status=0|1&barcode=11111
     */
    static String URL_UPDATE_STATUS = URL_INVSKUSTORE + "updateStatus";

    /**
     * 商品和货架绑定
     * /invSkuStore/bindRackNo?barcode=998800000000&rackNo=00102，
     */
    static String URL_GOODSSKU_BINDRACKNO = URL_INVSKUSTORE + "bindRackNo";

    /**
     * 库存转换，其中的id都是库存sku商品id
     * /invSkuStore/changeSkuStore?
     * sendItems=[{"id":111,"quantity":1.0},
     * {"id":222,"quantity":1.0}]
     * &receiveItem={"id":333,"quantity":1.0}
     */
    static String URL_INVSKUSTORE_CHANGESKUSTORE = URL_INVSKUSTORE + "changeSkuStore";

    public static void register() {
        URL_INVSKUSTORE = MfhApi.URL_BASE_SERVER + "/invSkuStore/";

        URL_UPDATE_STATUS = URL_INVSKUSTORE + "updateStatus";

        URL_GOODSSKU_BINDRACKNO = URL_INVSKUSTORE + "bindRackNo";

        URL_INVSKUSTORE_CHANGESKUSTORE = URL_INVSKUSTORE + "changeSkuStore";
    }

}
