package com.mfh.litecashier.utils;


import android.graphics.Color;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.CashierApp;

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
        //保存应用启动时间
        SharedPreferencesManager.setAppStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(currentDate));
        //设置应用当天首次启动时间
        String appDayFirstStartupDatetime = SharedPreferencesManager.getAppDayFirstStartupDateTime();
        if (StringUtils.isEmpty(appDayFirstStartupDatetime)) {
            SharedPreferencesManager.setAppDayFirstStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(currentDate));
        } else {
            //比较当前启动时间和应用当天首次启动时间是否是同一天
            Date appDayFirstStartupDate = null;
            try {
                appDayFirstStartupDate = TimeCursor.FORMAT_YYYYMMDDHHMMSS.parse(appDayFirstStartupDatetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!TimeUtil.isSameDay(currentDate, appDayFirstStartupDate)) {
                SharedPreferencesManager.setAppDayFirstStartupDateTime(TimeCursor.InnerFormat.format(currentDate));
            }
        }
        ZLogger.d(String.format("Initialize--application startup datetime: %s application day first startup datetime: %s",
                SharedPreferencesManager.getAppStartupDateTime(), SharedPreferencesManager.getAppDayFirstStartupDateTime()));

    }

    /**
     * 获取当天应用程序启动日期
     */
    public static Date getAppDayFirstStartupDateTime() {
        Date date = new Date();

        String appDayFirstStartupDateTime = SharedPreferencesManager.getAppDayFirstStartupDateTime();
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

    public static void clearCache() {
        ZLogger.d("清空缓存");
        DataCleanManager.cleanDatabases(CashierApp.getAppContext());
        //清除数据缓存
        DataCleanManager.clearCache(CashierApp.getAppContext());

        //清除编辑器保存的临时内容Properties
        //清除webview缓存
//        WebViewUtils.clearCacheFolder();

        //清除图片缓存
//        Glide.get(CashierApp.getAppContext()).clearMemory();

        DataCacheHelper.getInstance().reset();
    }

    /**
     * 清空缓存数据
     */
    public static void clearTempData() {
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERECEIPT_GOODS_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_ORDER_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_CREATERETURN_SUPPLY_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_PURCHASE_SEARCH_PARAMS);
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
     * */
    public static int getOkTextColor(){
        return Color.parseColor("#FF009B4E");
    }
    /**
     * 获取错误状态的文字颜色
     * */
    public static int getErrorTextColor(){
        return Color.parseColor("#FE5000");
    }


}
