package com.bingshanguxue.pda.utils;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by bingshanguxue on 10/11/2016.
 */

public class SharedPreferencesManagerImpl extends SharedPreferencesManager{
    public static final String RELEASE_PREFIX = "release_pda";
    public static final String DEV_PREFIX = "dev_pda";
    public static String PREF_NAME_PREFIX = "pda";


    public static final String KEY_B_CAMERASWEEP_ENABLED = "isCameraSweepEnabled";
    public static final String PREF_KEY_STOCKTAKE_LAST_UPDATE = "stocktake_lastUpdate";//最后一次更新时间
    public static final String PREF_KEY_STOCKTAKE_LAST_ORDERID = "PREF_KEY_STOCKTAKE_LAST_ORDERID";// 上一次盘点批次编号
    public static final String PK_SYNC_PRODUCTS_STARTCURSOR = "pk_sync_products_startcursor";//时间戳
    public static final String PK_SYNC_PRODUCTSKU_STARTCURSOR = "pk_sync_productsku_startcursor";
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


    public static void setCameraSweepEnabled(boolean enabled){
        set(PREF_NAME_CONFIG, KEY_B_CAMERASWEEP_ENABLED, enabled);
    }

    public static boolean isCameraSweepEnabled(){
        return getBoolean(PREF_NAME_CONFIG, KEY_B_CAMERASWEEP_ENABLED, false);
    }

    public static String getSyncProductsStartcursor(){
        return getText(dynamicRegister(), PK_SYNC_PRODUCTS_STARTCURSOR, "");
    }

    public static void setSyncProductsStartcursor(String cursor){
        set(dynamicRegister(), PK_SYNC_PRODUCTS_STARTCURSOR, cursor);
    }


    public static String getStocktakeLastUpdate() {
        return getText(PREF_KEY_STOCKTAKE_LAST_UPDATE, "");
    }

    public static String getStocktakeLastUpdate2() {
        String lastCursor = getText(PREF_KEY_STOCKTAKE_LAST_UPDATE, "");
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
        set(PREF_KEY_STOCKTAKE_LAST_UPDATE, lastUpdate);
    }

    public static void setStocktakeLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            set(PREF_KEY_STOCKTAKE_LAST_UPDATE, TimeCursor.InnerFormat.format(lastUpdate));
        }
    }

    public static void setLastStocktakeOrderId(Long orderId) {
        set(PREF_KEY_STOCKTAKE_LAST_ORDERID, orderId);
    }

    public static Long getLastStocktakeOrderId() {
        return getLong(PREF_KEY_STOCKTAKE_LAST_ORDERID, 0L);
    }

    /**
     * 前缀＋公司编号＋部门编号
     * 使用静态数据会导致数据不能同步
     */
    public static String dynamicRegister() {
        return String.format("%s_%d_%d", PREF_NAME_PREFIX, MfhLoginService.get().getSpid(),
                MfhLoginService.get().getCurOfficeId());
    }


    public static String getText(String key) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return getText(dynamicRegister(), key, "");
    }

    public static String getText(String key, String defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return getText(dynamicRegister(), key, defVal);
    }


    public static int getInt(String key, int defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return getInt(dynamicRegister(), key, defVal);
    }

    public static Long getLong(String key, Long defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return getLong(dynamicRegister(), key, defVal);
    }
    public static boolean getBoolean(String key, boolean defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
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
