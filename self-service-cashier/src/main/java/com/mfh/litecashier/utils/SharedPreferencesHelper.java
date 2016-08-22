package com.mfh.litecashier.utils;

import android.content.SharedPreferences;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.SharedPreferencesUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.ParseException;
import java.util.Date;


/**
 * <ol>
 * Preference Key (简称PK，命名格式也是以PK开头)
 * <li>命名格式: PK_[数据类型]_[功能模块]_[方法名]_[辅助参数]</li>
 * <li>数据类型(DATATYPE)
 * <table>
 * <th>PK_[DATATYPE]</th>
 * <tr>
 * <td>PK_S</td>
 * <td>PK_B</td>
 * <td>PK_I</td>
 * <td>PK_L</td>
 * </tr>
 * <tr>
 * <td>String</td>
 * <td>boolean/Boolean</td>
 * <td>int/Integer</td>
 * <td>long/Long</td>
 * </tr>
 * </table>
 * </li>
 * <li>功能模块(DOMAIN)</li>
 * <p/>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 2015/6/17.<br>
 */
public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";

    public static final String RELEASE_PREFIX = "release_litecashier";
    public static final String DEV_PREFIX = "dev_litecashier";
    public static String PREF_NAME_PREFIX = "litecashier";

    //导入商品批发商数据
    public static final String PK_S_IMPORT_FROMCHAINSKU_STARTCURSOR = "pk_import_from_startcursor";//时间戳

    //同步商品
    public static final String PK_SKU_UPDATE_UNREADNUMBER = "pk_sku_update_unreadnumber";   //上一次交接班班次
    private static final String PK_SYNC_PRODUCTS_STARTCURSOR = "pk_sync_products_startcursor";//时间戳
    private static final String PK_SYNC_PRODUCTS_MODE = "pk_sync_products_mode";//同步方式：0全量，1增量。
    private static final String PK_SYNC_PRODUCTSKU_STARTCURSOR = "pk_sync_productsku_startcursor";
    public static final String PK_SYNC_PRODUCTCATALOG_STARTCURSOR = "pk_sync_PRODUCTCATALOG_STARTCURSOR";
    //同步订单
    private static final String PK_POS_ORDER_LAST_UPDATE = "pos_order_lastUpdate";//最后一次更新时间
    private static final String PK_SYNC_ORDER_INTERVAL = "pk_sync_order_interval";//同步间隔（单位，秒）
    //同步账号
    private static final String PK_SYNC_COMPANY_HUMAN_INTERVAL = "pk_sync_company_human_interval";//同步间隔（单位，秒）
    private static final String PK_SYNC_COMPANY_HUMAN_ENABLED = "pk_sync_company_human_enabled";
   // 后台商品类目
    public static final String PK_SYNC_BACKEND_CATEGORYINFO_ENABLED = "pk_sync_bakend_categoryinfo_enabled";
    public static final String PK_SYNC_BACKEND_CATEGORYINFO_FRESH_ENABLED = "pk_sync_bakend_categoryinfo_fresh_enabled";
      //库存
    public static final String PK_SYNC_STOCKCHECK_ORDER_ENABLED = "pk_sync_stockcheck_order_enabled";//库存盘点订单
    public static final String PK_SYNC_STOCKLOSS_ORDER_ENABLED = "pk_sync_stockloss_order_enabled";//库存报损订单
    public static final String PK_SYNC_INVIOORDER_IN_ENABLED = "pk_sync_invioorder_in_enabled";//库存批次流水（入库）
    public static final String PK_SYNC_INVIOORDER_OUT_ENABLED = "pk_sync_invioorder_out_enabled";//库存批次流水（出库）
    public static final String PK_SYNC_INVTRANSORDER_IN_ENABLED = "pk_sync_invtransorder_in_enabled";//库存调拨订单（调入）
    public static final String PK_SYNC_INVTRANSORDER_OUT_ENABLED = "pk_sync_invtransorder_out_enabled";//库存调拨订单（调出）
    //采购
    public static final String PK_SYNC_PURCHASESEND_ORDER_ENABLED = "pk_sync_purchasesend_order_enabled";//采购订单
    public static final String PK_SYNC_PURCHASERECEIPT_ORDER_ENABLED = "pk_sync_purchasereceipt_order_enabled";//采购收货订单
    public static final String PK_SYNC_PURCHASERETURN_ORDER_ENABLED = "pk_sync_purchasereturn_order_enabled";//采购退货订单
    //前台类目子类目
    private static final String PK_SYNC_FRONTCATEGORY_SUB_ENABLED = "pk_sync_frontcategory_sub_enabled";
    //交接班
    public static final String PK_LAST_HANDOVER_DATETIME = "pk_last_handover_datetime";  //上一次交接班时间
    public static final String PK_LAST_HANDOVER_SHIFTID = "pk_last_handover_shiftid";   //上一次交接班班次

    //订单
    public static final String PK_ONLINE_FRESHORDER_UNREADNUMBER = "pk_online_freshorder_unreadnumber";   //上一次交接班班次

    //银联
    public static final String PK_UMSIPS_IP = "pk_umsips_ip";       //主机IP
    public static final String PK_UMSIPS_PORT = "pk_umsips_port";     //主机端口号
    public static final String PK_UMSIPS_MCHTID = "pk_umsips_mchtid";   //商户号
    public static final String PK_UMSIPS_TERMID = "pk_umsips_termid";   //终端设备号
    public static final String PK_UMSIPS_AUTHSN = "pk_umsips_authsn";   //SN密文

    //寺冈电子秤

    //混合支付
    public static final String PREF_KEY_HYBRID_PAYMENT_ENABLED = "pk_hybrid_payment_enabled";
    //串口客显
    public static final String PREF_KEY_COM_CUSTOMERDISPLAY_ENABLED = "pk_com_customerdisplay_enabled";
    //PAD客显
    public static final String PREF_KEY_PAD_CUSTOMERDISPLAY_ENABLED = "pk_pad_customerdisplay_enabled";

    public static String prefName = TAG;

    public static SharedPreferences getPreferences(String prefName) {
        return SharedPreferencesUtil.getSharedPreferences(MfhApplication.getAppContext(),
                prefName);
    }


    public static String getSyncProductsCursor() {
        return getText(PK_SYNC_PRODUCTS_STARTCURSOR);
    }

    public static void setSyncProductsCursor(String lastUpdate) {
        set(PK_SYNC_PRODUCTS_STARTCURSOR, lastUpdate);
    }

    public static void setSyncProductsCursor(Date lastUpdate) {
        if (lastUpdate != null) {
            set(PK_SYNC_PRODUCTS_STARTCURSOR, TimeCursor.InnerFormat.format(lastUpdate));
        }
    }

    /**
     * @return 同步方式：0全量, 1增量。
     */
    public static int getSyncProductsMode() {
        return getInt(PK_SYNC_PRODUCTS_MODE, 0);
    }

    /**
     * 同步方式：0全量，1增量。
     */
    public static void setSyncProductsMode(int mode) {
        set(PK_SYNC_PRODUCTS_MODE, mode);
    }

    public static String getSyncProductSkuCursor() {
        return getText(PK_SYNC_PRODUCTSKU_STARTCURSOR, "");
    }

    public static void setPosSkuLastUpdate(String lastUpdate) {
        set(PK_SYNC_PRODUCTSKU_STARTCURSOR, lastUpdate);
    }

    public static void setPosSkuLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            set(PK_SYNC_PRODUCTSKU_STARTCURSOR, TimeCursor.InnerFormat.format(lastUpdate));
        }
    }

    public static String getPosOrderLastUpdate() {
        return getText(PK_POS_ORDER_LAST_UPDATE, "");
    }

    public static String getUploadOrderLastUpdate() {
        String lastCursor = getText(PK_POS_ORDER_LAST_UPDATE);
        ZLogger.d(String.format("上次订单更新时间(%s)。", lastCursor));

        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(lastCursor)) {
            //得到指定模范的时间
            try {
                Date d1 = TimeCursor.InnerFormat.parse(lastCursor);
                Date d2 = new Date();
                if (d1.compareTo(d2) > 0) {
                    lastCursor = TimeCursor.InnerFormat.format(d2);
//                    SharedPreferencesHelper.setPosOrderLastUpdate(d2);
                    ZLogger.d(String.format("上次订单更新时间大于当前时间，使用当前时间(%s)。", lastCursor));
                }
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.e(e.toString());
            }
        }

        return lastCursor;
    }

    public static void setPosOrderLastUpdate(String lastUpdate) {
        set(PK_POS_ORDER_LAST_UPDATE, lastUpdate);
    }

    public static void setPosOrderLastUpdate(Date lastUpdate) {
        if (lastUpdate != null) {
            set(PK_POS_ORDER_LAST_UPDATE, TimeCursor.InnerFormat.format(lastUpdate));
        } else {
            set(PK_POS_ORDER_LAST_UPDATE, "");
        }
    }

    /**
     * 获取订单同步时间间隔，默认30*1000秒/次
     */
    public static int getSyncCompanyHumanInterval() {
        return getInt(PK_SYNC_COMPANY_HUMAN_INTERVAL, 30 * 60);
    }

    public static void setSyncCompanyHumanInterval(int seconds) {
        set(PK_SYNC_COMPANY_HUMAN_INTERVAL, seconds);
    }

    public static boolean isSyncCompanyHumanEnabled() {
        return getBoolean(PK_SYNC_COMPANY_HUMAN_ENABLED, true);
    }

    public static void setSyncCompanyHumanEnabled(boolean enabled) {
        set(PK_SYNC_COMPANY_HUMAN_ENABLED, enabled);
    }

    public static boolean isSyncFrontCategorySubEnabled() {
        return getBoolean(PK_SYNC_FRONTCATEGORY_SUB_ENABLED, true);
    }

    public static void setSyncFrontCategorySubEnabled(boolean enabled) {
        set(PK_SYNC_FRONTCATEGORY_SUB_ENABLED, enabled);
    }

    /**
     * 获取上一次交接班班次
     */
    public static int getLastHandoverShiftId() {
        return getInt(PK_LAST_HANDOVER_SHIFTID, 0);
    }


    public static void setLastHandoverShiftId(int shiftId) {
        set(PK_LAST_HANDOVER_SHIFTID, shiftId);
    }

    /**
     * 获取上一次交接班时间
     */
    public static Date getLastHandoverDateTime() {
        Date date = new Date();

        String lastCursor = getLastHandoverDateTimeStr();
        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(lastCursor)) {
            //得到指定模范的时间
            try {
                date = TimeCursor.InnerFormat.parse(lastCursor);
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.e(e.toString());
            }
        }

        return date;
    }

    /**
     * 获取上一次交接班时间,如果上一次交接班时间为空，则使用当前首次启动时间作为上一次交接班时间
     */
    public static String getLastHandoverDateTimeStr() {
        String appDayFirstStartupDateTime = SharedPreferencesManager.getAppDayFirstStartupDateTime();
        String lastCursor = getText(PK_LAST_HANDOVER_DATETIME, appDayFirstStartupDateTime);
        if (StringUtils.isEmpty(lastCursor)) {
            return appDayFirstStartupDateTime;
        }
        return lastCursor;
    }

    /**
     * 前缀＋公司编号＋部门编号
     * 使用静态数据会导致数据不能同步
     */
    public static String register() {
        return String.format("%s_%d_%d", PREF_NAME_PREFIX, MfhLoginService.get().getSpid(),
                MfhLoginService.get().getCurOfficeId());
    }


    public static String getText(String key) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                register(), key, "");
    }

    public static String getText(String key, String defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                register(), key, defVal);
    }


    public static int getInt(String key, int defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.getInt(MfhApplication.getAppContext(),
                register(), key, defVal);
    }

    public static Long getLong(String key, Long defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.getLong(MfhApplication.getAppContext(),
                register(), key, defVal);
    }
    public static boolean getBoolean(String key, boolean defVal) {
//        ZLogger.d(String.format("getPosOrderLastUpdate(%s)", prefName));
        return SharedPreferencesUtil.get(MfhApplication.getAppContext(),
                register(), key, defVal);
    }

    public static void set(String key, String value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), register(), key, value);
    }
    public static void set(String key, int value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), register(), key, value);
    }
    public static void set(String key, Long value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), register(), key, value);
    }
    public static void set(String key, boolean value) {
        SharedPreferencesUtil.set(MfhApplication.getAppContext(), register(), key, value);
    }



}
