package com.mfh.litecashier.utils;

import com.mfh.framework.core.utils.ACache;
import com.mfh.litecashier.CashierApp;

/**
 * Created by bingshanguxue on 5/5/16.
 */
public class ACacheHelper {
    public static String CACHE_NAME = "ACache";
    public static final String CK_FRONT_CATEGORY_ID = "CK_FRONT_CATEGORY_ID";//前台类目，format:CACHE_KEY_FRONT_CATEGORY_1111，其中1111是类目的id
    public static final String CK_FRONT_CATEGORY_GOODS = "CK_FRONT_CATEGORY_GOODS";//前台类目商品，format:CACHE_KEY_FRONT_CATEGORY_GOODS_1111_222，其中1111是类目的id
    //后台类目树
    public static final String CK_STOCKGOODS_CATEGORY = "CK_STOCKGOODS_CATEGORY";//商品后台类目
    //订单流水
    public static final String CK_ORDERFLOW_STORE = "CK_ORDERFLOW_STORE";// 线下门店
    public static final String CK_ORDERFLOW_ONLINE = "CK_ORDERFLOW_ONLINE";// 线上订单
    public static final String CK_ORDERFLOW_LAUNDRY = "CK_ORDERFLOW_LAUNDRY";// 衣服洗护
    public static final String CK_ORDERFLOW_EXPRESS_DELIVERY = "CK_ORDERFLOW_EXPRESS_DELIVERY";// 代收快递
    public static final String CK_ORDERFLOW_EXPRESS = "CK_ORDERFLOW_EXPRESS";// 代寄快递
    //采购
    public static final String CK_PURCHASE_ORDER = "CK_PURCHASE_ORDER";   // 采购订单
    public static final String CK_PURCHASE_RECEIPT = "CK_PURCHASE_RECEIPT";// 采购收货
    public static final String CK_PURCHASE_RETURN= "CK_PURCHASE_RETURN";// 采购退货
    //库存
    public static final String CK_STOCK_CHECK_ORDER = "CK_STOCK_CHECK_ORDER";//库存盘点订单
    public static final String CK_STOCK_LOSS_ORDER = "CK_STOCK_LOSS_ORDER";//库存报损订单
    public static final String CK_INVENTORY_TRANS = "CK_INVENTORY_TRANS";//库存调拨订单
    public static final String CK_INVENTORY_IO = "CK_INVENTORY_IO";//批次流水
    //其他
    public static final String CK_RELATIVE_TENANT = "CK_RELATIVE_TENANT";//关联租户
    public static final String CK_PLATFORM_PROVIDER = "CK_PLATFORM_PROVIDER";//批发商
    //临时变量
    public static final String TCK_PURCHASE_CREATERECEIPT_ORDER_DATA = "TCK_PURCHASE_CREATERECEIPT_ORDER_DATA";//新建收货单临时存储数据
    public static final String TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA = "TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA";//新建收货单临时存储数据
    public static final String TCK_PURCHASE_CREATERECEIPT_GOODS_DATA = "TCK_PURCHASE_CREATERECEIPT_GOODS_DATA";//新建收货单临时存储数据
    public static final String TCK_PURCHASE_CREATERETURN_ORDER_DATA = "TCK_PURCHASE_CREATERETURN_ORDER_DATA";//新建退货单临时存储数据
    public static final String TCK_PURCHASE_CREATERETURN_SUPPLY_DATA = "TCK_PURCHASE_CREATERETURN_SUPPLY_DATA";//新建退货单临时存储数据
    public static final String TCK_PURCHASE_CREATERETURN_GOODS_DATA = "TCK_PURCHASE_CREATERETURN_GOODS_DATA";//新建退货单临时存储数据
    public static final String TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA = "TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA";//新建调拨单单临时存储数据
    public static final String TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA = "TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA";//新建调拨单单临时存储数据
    public static final String TCK_PURCHASE_SEARCH_PARAMS = "TCK_PURCHASE_SEARCH_PARAMS";//采购商品搜索条件


    public static void put(String key, String value){
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .put(key, value);
    }

    public static String getAsString(String key){
        return ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .getAsString(key);
    }

    public static boolean remove(String key){
        return ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME)
                .remove(key);
    }

    public static void clear(){
        ACache.get(CashierApp.getAppContext(), ACacheHelper.CACHE_NAME).clear();
    }


}
