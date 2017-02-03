package com.mfh.litecashier.utils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.database.service.PosLocalCategoryService;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.cashier.database.service.PosProductSkuService;
import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.bingshanguxue.cashier.database.service.ProductCatalogService;
import com.bingshanguxue.cashier.hardware.scale.ScaleAgent;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.ui.activity.SplashActivity;

import net.tsz.afinal.FinalDb;

import org.century.GreenTagsApi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.Date;


/**
 * 应用程序工具包
 * Created by bingshanguxue on 2015/5/11.
 */
public class AppHelper {

    /**
     * 保存应用程序启动日期和时间
     */
    public static void saveAppStartupDatetime() {
        Date currentDate = new Date();
        //设置应用当天首次启动时间
        String appDayFirstStartupDatetime = SharedPrefesManagerFactory.getAppDayFirstStartupDateTime();
        if (StringUtils.isEmpty(appDayFirstStartupDatetime)) {
            SharedPrefesManagerFactory.setAppDayFirstStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(currentDate));
        } else {
            //比较当前启动时间和应用当天首次启动时间是否是同一天
            Date appDayFirstStartupDate = null;
            try {
                appDayFirstStartupDate = TimeCursor.FORMAT_YYYYMMDDHHMMSS.parse(appDayFirstStartupDatetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!TimeUtil.isSameDay(currentDate, appDayFirstStartupDate)) {
                SharedPrefesManagerFactory.setAppDayFirstStartupDateTime(TimeCursor.InnerFormat.format(currentDate));
            }
        }
        ZLogger.d(String.format("application startup datetime: %s application day first startup datetime: %s",
                SharedPrefesManagerFactory.getAppStartupDateTime(),
                SharedPrefesManagerFactory.getAppDayFirstStartupDateTime()));

    }

    /**
     * 获取当天应用程序启动日期
     */
    public static Date getAppDayFirstStartupDateTime() {
        Date date = new Date();

        String appDayFirstStartupDateTime = SharedPrefesManagerFactory.getAppDayFirstStartupDateTime();
        //与当前时间相比，取最小当时间
        if (!StringUtils.isEmpty(appDayFirstStartupDateTime)) {
            //得到指定模范的时间
            try {
                date = TimeCursor.InnerFormat.parse(appDayFirstStartupDateTime);
            } catch (ParseException e) {
//            e.printStackTrace();
                ZLogger.e(e.toString());
            }
        }

        return date;
    }

    /**
     * 清空注册用户账户数据
     */
    public static void resetMemberAccountData() {
        try {
//            //清空挂起订单和当前订单
//            HangUpOrderService.get().clear();
//            CashierProductService.get().clear();

            MfhLoginService.get().clear();
            ServiceFactory.cleanService();
//            UserProfileHelper.cleanUserProfile();//清空个人信息
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
//
//    /**
//     * 广播微信支付结果
//     * */
//    public static void broadcastWXPayResp(int errCode, String errStr){
//        Bundle extras = new Bundle();
//        extras.putInt(Constants.BROADCAST_KEY_WXPAY_RESP_ERRCODE, errCode);
//        extras.putString(Constants.BROADCAST_KEY_WXPAY_RESP_ERRSTR, errStr);
//
//        if(HybridActivity.getInstance() != null){
//            HybridActivity.getInstance().parseWxpayResp(extras);
//        }
//        UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_WXPAY_RESP);
//
////        No subscribers registered for event class com.mfh.cashier.events.WxPayEvent
//        EventBus.getDefault().post(
//                new WxPayEvent(errCode, errStr));
//    }

    /**
     * 关闭App
     * */
    public static void closeApp(){
        ZLogger.d("准备关闭App...");
        String dbName;
        if (BizConfig.RELEASE) {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_release.db");
        } else {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_cashier_dev.db");
        }
        ZLogger.d("关闭数据库:" + dbName);
        FinalDb db = FinalDb.getDb(dbName);
        if (db != null) {
            db.close();
        }
        System.exit(0);
    }

    /**
     * 重启APP
     * */
    public static void restartApp(Context context){
        if (context == null){
            return;
        }
        Intent mStartActivity = new Intent(context, SplashActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,
                mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        // full restart and initialize the application
        System.exit(0);
    }

    /**
     * 恢复出厂设置
     * */
    public static void resetFactoryData(Context context){
        clearAppData();

        //删除SharedPreference
        SharedPrefesManagerFactory.clear();
        SharedPrefesManagerFactory.clear(SharedPrefesManagerBase.PREF_NAME_APP);

        SharedPrefesManagerFactory.clear(SerialManager.PREF_NAME_SERIAL);
        SharedPrefesManagerFactory.clear(SMScaleSyncManager2.PREF_SMSCALE);
        SharedPrefesManagerFactory.clear(GreenTagsApi.PREF_GREENTAGS);
        SharedPrefesManagerFactory.clear(ScaleAgent.PREF_NAME);

        //删除无效文件
        clearRedunantData(true);

        //删除数据库
        DataCleanManager.cleanApplicationData(context);

        //删除用户账号数据
        MfhLoginService.get().clear();

        restartApp(context);

    }

    /**
     * 清除数据（文件，设置，账户，数据库等）
     * <ol>
     * <li>应用首次启动</li>
     * <li>切换账号</li>
     * </ol>
     */
    public static void clearAppData() {
        CashierShopcartService.getInstance().clear();
        PosCategoryGodosTempService.getInstance().clear();
        PosProductService.get().clear();//商品库
        PosProductSkuService.get().clear();//一品多码
        PosLocalCategoryService.get().clear();//前台类目关联商品
        ProductCatalogService.getInstance().clear();//前台类目
        SharedPreferencesUltimate.setSyncProductsCursor("");
        SharedPreferencesUltimate.setPosSkuLastUpdate("");
        SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_SYNC_PRODUCTCATALOG_STARTCURSOR,
                "");

        clearRedunantData(false);
    }

    /**
     * 清空过期数据，保留最近7天的数据。
     */
    public static void clearRedunantData(boolean isFactoryReset){
        if (isFactoryReset){
            CashierHelper.clearOldPosOrder(0);//收银订单
            PosTopupService.get().deleteOldData(0);
            ZLogger.deleteOldFiles(0);
            SMScaleSyncManager2.deleteOldFiles(0);
            EmbMsgService.getInstance().clearReduantData(0);


            //删除缓存
            ACacheHelper.clear();
            //清除数据缓存
            DataCleanManager.clearCache(CashierApp.getAppContext());
            GlobalInstance.getInstance().reset();
        }
        else{
            CashierHelper.clearOldPosOrder(14);//收银订单
            PosTopupService.get().deleteOldData(7);
            ZLogger.deleteOldFiles(7);
            SMScaleSyncManager2.deleteOldFiles(1);
            EmbMsgService.getInstance().clearReduantData(7);

            AppHelper.clearCacheData();
        }

    }

    /**
     * 清空缓存数据
     */
    public static void clearCacheData() {
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_GOODS_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);

        //清除数据缓存
        DataCleanManager.clearCache(CashierApp.getAppContext());

        //清除编辑器保存的临时内容Properties
        //清除webview缓存
//        WebViewUtils.clearCacheFolder();

        //清除图片缓存
//        Glide.get(CashierApp.getAppContext()).clearMemory();

        GlobalInstance.getInstance().reset();
    }
//
//    /**
//     * 生成16进制累加和校验码
//     *
//     * @param data 除去校验位的数据
//     * @return
//     */
//    public static String makeChecksum(String data) {
//        if (StringUtils.isEmpty(data)) {
//            return "";
//        }
//        int total = 0;
//        int len = data.length();
//        int num = 0;
//        while (num < len) {
//            String s = data.substring(num, num + 2);
//            System.out.println(s);
//            total += Integer.parseInt(s, 16);
//            num = num + 2;
//        }
//        /**
//         * 用256求余最大是255，即16进制的FF
//         */
//        int mod = total % 256;
//        String hex = Integer.toHexString(mod);
//        len = hex.length();
//        //如果不够校验位的长度，补0,这里用的是两位校验
//        if (len < 2) {
//            hex = "0" + hex;
//        }
//        return hex;
//    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

//    try {
//        System.out.println(getMD5Checksum("apache-tomcat-5.5.17.exe"));
//        // output :
//        //  0bb2827c5eacf570b6064e24e0e6653b
//        // ref :
//        //  http://www.apache.org/dist/
//        //          tomcat/tomcat-5/v5.5.17/bin
//        //              /apache-tomcat-5.5.17.exe.MD5
//        //  0bb2827c5eacf570b6064e24e0e6653b *apache-tomcat-5.5.17.exe
//    }
//    catch (Exception e) {
//        e.printStackTrace();
//    }

    /**
     * 获取正确状态的文字颜色
     */
    public static int getOkTextColor() {
        return Color.parseColor("#FF009B4E");
    }

    /**
     * 获取错误状态的文字颜色
     */
    public static int getErrorTextColor() {
        return Color.parseColor("#FE5000");
    }


}
