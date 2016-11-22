package com.bingshanguxue.pda.utils;

import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by bingshanguxue on 10/11/2016.
 */

public class SharedPrefesManagerUltimate extends SharedPrefesManagerBase {
    //和用户相关
    public static final String PREF_KEY_STOCKTAKE_LAST_UPDATE = "stocktake_lastUpdate";//最后一次更新时间
    public static final String PREF_KEY_STOCKTAKE_LAST_ORDERID = "PREF_KEY_STOCKTAKE_LAST_ORDERID";// 上一次盘点批次编号
    public static final String PK_SYNC_PRODUCTS_STARTCURSOR = "pk_sync_products_startcursor";//商品档案时间戳
    public static final String PK_SYNC_PRODUCTSKU_STARTCURSOR = "pk_sync_productsku_startcursor";//一品多吗
    public static final String PK_S_POSORDER_SYNC_STARTCURSOR = "pos_order_lastUpdate";//最后一次更新时间


    //    private static SharedPreferencesHelper instance = null;
//    /**
//     * 返回 SharedPreferencesHelper 实例
//     * @return
//     */
//    public static SharedPreferencesHelper getInstance() {
//
//        if (instance == null) {
//            synchronized (SharedPreferencesHelper.class) {
//                if (instance == null) {
//                    instance = new SharedPreferencesHelper();
//                }
//            }
//        }
//        return instance;
//    }


    public static String getSyncProductsStartcursor(){
        return getString(dynamicRegister(),  PK_SYNC_PRODUCTS_STARTCURSOR, "");
    }

    public static void setSyncProductsStartcursor(String cursor){
        set(dynamicRegister(),  PK_SYNC_PRODUCTS_STARTCURSOR, cursor);
    }

    public static String getSyncProductSkuCursor() {
        return getString(dynamicRegister(), PK_SYNC_PRODUCTSKU_STARTCURSOR, "");
    }

    public static void setSyncProductSkuStartcursor(String cursor){
        set(dynamicRegister(), PK_SYNC_PRODUCTSKU_STARTCURSOR, cursor);
    }

    public static void setSyncProductSkuStartcursor(Date lastCursor) {
        set(dynamicRegister(), PK_SYNC_PRODUCTSKU_STARTCURSOR,
                TimeUtil.format(lastCursor, TimeUtil.FORMAT_YYYYMMDDHHMMSS));

    }

    public static String getPosOrderLastUpdate() {
        return getString(dynamicRegister(), PK_S_POSORDER_SYNC_STARTCURSOR, "");
    }


    public static void setPosOrderLastUpdate(String lastUpdate) {
        set(dynamicRegister(), PK_S_POSORDER_SYNC_STARTCURSOR, lastUpdate);
    }

    public static void setPosOrderLastUpdate(Date lastUpdate) {
        set(dynamicRegister(), PK_S_POSORDER_SYNC_STARTCURSOR,
                TimeUtil.format(lastUpdate, TimeUtil.FORMAT_YYYYMMDDHHMMSS));
    }


    public static String getStocktakeLastUpdate() {
        return getString(dynamicRegister(), PREF_KEY_STOCKTAKE_LAST_UPDATE, "");
    }

    public static String getStocktakeLastUpdate2() {
        String lastCursor = getStocktakeLastUpdate();
        ZLogger.d(String.format("DataSync--上次订单更新时间(%s)。", lastCursor));
        //得到指定模范的时间
        try {
            Date d1 = TimeCursor.InnerFormat.parse(lastCursor);
            Date d2 = new Date();
            if (d1.compareTo(d2) > 0) {
                lastCursor = TimeCursor.InnerFormat.format(d2);
                setStocktakeLastUpdate(d2);
                ZLogger.d(String.format("DataSync--上次订单更新时间大于当前时间，使用当前时间(%s)。", lastCursor));
            }
        } catch (ParseException e) {
//            e.printStackTrace();
            ZLogger.e(e.toString());
        }

        return lastCursor;
    }

    public static void setStocktakeLastUpdate(String lastUpdate) {
        set(dynamicRegister(), PREF_KEY_STOCKTAKE_LAST_UPDATE, lastUpdate);
    }

    public static void setStocktakeLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            set(dynamicRegister(), PREF_KEY_STOCKTAKE_LAST_UPDATE, TimeCursor.InnerFormat.format(lastUpdate));
        }
    }

    public static void setLastStocktakeOrderId(Long orderId) {
        set(dynamicRegister(), PREF_KEY_STOCKTAKE_LAST_ORDERID, orderId);
    }

    public static Long getLastStocktakeOrderId() {
        return getLong(dynamicRegister(), PREF_KEY_STOCKTAKE_LAST_ORDERID, 0L);
    }

    /**
     * 前缀＋公司编号＋部门编号
     * 使用静态数据会导致数据不能同步
     */
    public static String dynamicRegister() {
        return String.format("%s_%d_%d", PREF_NAME_APP, MfhLoginService.get().getSpid(),
                MfhLoginService.get().getCurOfficeId());
    }

    public static String getText(String key) {
        return getString(dynamicRegister(), key, "");
    }

    public static String getText(String key, String defVal) {
        return getString(dynamicRegister(), key, defVal);
    }


    public static int getInt(String key, int defVal) {
        return getInt(dynamicRegister(), key, defVal);
    }

    public static Long getLong(String key, Long defVal) {
        return getLong(dynamicRegister(), key, defVal);
    }
    public static boolean getBoolean(String key, boolean defVal) {
        return getBoolean(dynamicRegister(), key, defVal);
    }

    public static void set(String key, String value) {
        set(dynamicRegister(), key, value);
    }
    public static void set(String key, int value) {
        set(dynamicRegister(), key, value);
    }
    public static void set(String key, Long value) {
        set(dynamicRegister(), key, value);
    }
    public static void set(String key, boolean value) {
        set(dynamicRegister(), key, value);
    }



}
