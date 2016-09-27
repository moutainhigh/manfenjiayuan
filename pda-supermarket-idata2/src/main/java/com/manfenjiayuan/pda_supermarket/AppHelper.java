package com.manfenjiayuan.pda_supermarket;


import android.graphics.Color;

import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.bingshanguxue.pda.utils.ACacheHelper;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_supermarket.database.logic.ShelveService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.utils.DataCleanManager;

import net.tsz.afinal.FinalDb;

import java.util.Calendar;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * Created by Nat on 2015/5/11.
 */
public class AppHelper {

//    /**
//     * 清空匿名用户账户数据
//     * */
//    public static void resetAnonymousAccountData(){
//
//    }
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
////        No subscribers registered for event class com.mfh.enjoycity.events.WxPayEvent
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
                    UConfig.CONFIG_PARAM_DB_NAME, "mfh_pda_supermarket_release.db");
        } else {
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_pda_supermarket_dev.db");
        }
        ZLogger.d("关闭数据库:" + dbName);
        FinalDb db = FinalDb.getDb(dbName);
        if (db != null) {
            db.close();
        }
        System.exit(0);
    }

    /**
     * 清空过期数据，保留最近7天的数据。
     */
    public static void clearRedunantData(boolean isFactoryReset){
        if (isFactoryReset){
            ZLogger.deleteOldFiles(0);
            EmbMsgService.getInstance().clearReduantData(0);


            //删除缓存
            ACacheHelper.clear();
            //清除数据缓存
            DataCleanManager.clearCache(AppContext.getAppContext());
        }
        else{
            ZLogger.deleteOldFiles(7);
            EmbMsgService.getInstance().clearReduantData(7);

            AppHelper.clearCacheData();
        }
    }

    /**
     * 清空缓存数据
     */
    public static void clearCacheData() {
        ACacheHelper.remove(ACacheHelper.INVRECV_INSPECT_GOODS_TEMPDATA);

        //清除数据缓存
        DataCleanManager.clearCache(AppContext.getAppContext());

        //清除编辑器保存的临时内容Properties
        //清除webview缓存
//        WebViewUtils.clearCacheFolder();

        //清除图片缓存
//        Glide.get(CashierApp.getAppContext()).clearMemory();
    }

    /**
     * 清除旧数据
     *
     * @param saveDate 保存的天数
     */
    public static void clearOldPosOrder(int saveDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//
        String expireCursor = TimeCursor.InnerFormat.format(calendar.getTime());
        ZLogger.d(String.format("订单过期时间(%s)保留最近30天数据。", expireCursor));

        //清除订单
        InvCheckGoodsService.get().deleteBy(String.format("updatedDate < '%s'", expireCursor));
        ShelveService.get().deleteBy(String.format("updatedDate < '%s'", expireCursor));
    }

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
