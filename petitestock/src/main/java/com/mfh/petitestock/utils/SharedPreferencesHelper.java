package com.mfh.petitestock.utils;

import android.content.SharedPreferences;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.SharedPreferencesUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.petitestock.AppContext;

import java.text.ParseException;
import java.util.Date;


/**
 * @author Nat.ZZN(bingshanguxue)
 *         Created by Nat.ZZN(bingshanguxue) on 2015/6/17.
 */
public class SharedPreferencesHelper {
    public static final String RELEASE_PREFIX = "release_petitestock";
    public static final String DEV_PREFIX = "dev_petitestock";
    public static String PREF_NAME_PREFIX = "petitestock";


    public static final String PREF_KEY_STOCKTAKE_LAST_UPDATE = "stocktake_lastUpdate";//最后一次更新时间
    public static final String PREF_KEY_STOCKTAKE_LAST_ORDERID = "PREF_KEY_STOCKTAKE_LAST_ORDERID";// 上一次盘点批次编号

    public static SharedPreferences getPreferences(String prefName) {
        return SharedPreferencesUtil.getSharedPreferences(AppContext.getAppContext(),
                prefName);
    }

    /**
     * 使用静态数据会导致数据不能同步
     */
    private static String getPosPrefName() {
        return String.format("%s_%d_%d", PREF_NAME_PREFIX, MfhLoginService.get().getSpid(),
                MfhLoginService.get().getCurOfficeId());
    }

    public static String getStocktakeLastUpdate() {
        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", getPosPrefName()));
        return SharedPreferencesUtil.get(AppContext.getAppContext(),
                getPosPrefName(), PREF_KEY_STOCKTAKE_LAST_UPDATE, "");
    }

    public static String getStocktakeLastUpdate2() {
        String lastCursor = SharedPreferencesUtil.get(AppContext.getAppContext(),
                getPosPrefName(), PREF_KEY_STOCKTAKE_LAST_UPDATE, "");
        ZLogger.d(String.format("DataSync--上次订单更新时间(%s)。", lastCursor));
        //得到指定模范的时间
        try {
            Date d1 = TimeCursor.InnerFormat.parse(lastCursor);
            Date d2 = new Date();
            if (d1.compareTo(d2) > 0) {
                lastCursor = TimeCursor.InnerFormat.format(d2);
                SharedPreferencesHelper.setStocktakeLastUpdate(d2);
                ZLogger.d(String.format("DataSync--上次订单更新时间大于当前时间，使用当前时间(%s)。", lastCursor));
            }
        } catch (ParseException e) {
//            e.printStackTrace();
            ZLogger.e(e.toString());
        }

        return lastCursor;
    }

    public static void setStocktakeLastUpdate(String lastUpdate) {
        ZLogger.d(String.format("setStocktakeLastUpdate(%s):%s", getPosPrefName(), lastUpdate));
        SharedPreferencesUtil.set(AppContext.getAppContext(), getPosPrefName(), PREF_KEY_STOCKTAKE_LAST_UPDATE, lastUpdate);
    }

    public static void setStocktakeLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            SharedPreferencesUtil.set(AppContext.getAppContext(), getPosPrefName(),
                    PREF_KEY_STOCKTAKE_LAST_UPDATE, TimeCursor.InnerFormat.format(lastUpdate));
        }
    }

    public static void setLastStocktakeOrderId(Long orderId) {
        ZLogger.d(String.format("setStocktakeLastUpdate(%s):%d", getPosPrefName(), orderId));
        SharedPreferencesUtil.set(AppContext.getAppContext(), getPosPrefName(), PREF_KEY_STOCKTAKE_LAST_ORDERID, orderId);
    }

    public static Long getLastStocktakeOrderId() {
        return SharedPreferencesUtil.getLong(AppContext.getAppContext(),
                getPosPrefName(), PREF_KEY_STOCKTAKE_LAST_ORDERID, 0L);
    }

    public static int getInt(String key, int defVal) {
        return SharedPreferencesUtil.getInt(AppContext.getAppContext(),
                getPosPrefName(), key, defVal);
    }

    public static void setInt(String key, int value) {
        SharedPreferencesUtil.set(AppContext.getAppContext(),
                getPosPrefName(), key, value);
    }

}
