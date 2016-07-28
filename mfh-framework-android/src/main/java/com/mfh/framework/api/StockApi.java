package com.mfh.framework.api;

/**
 * 库存Api
 * Created by bingshanguxue on 4/22/16.
 */
public class StockApi {

    //

    /**查找可出库的或已出库的包裹列表*/
    public static final String URL_STOCK_FIND_STOCKOUT = MfhApi.URL_BASE_SERVER + "/pmcstock/findStockOut";
    /**包裹出库操作(支持批量)*/
    public static final String URL_STOCK_OUT = MfhApi.URL_BASE_SERVER + "/pmcstock/stockOut";


}
