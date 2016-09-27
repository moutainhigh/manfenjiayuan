package com.manfenjiayuan.pda_wholesaler;


import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.bingshanguxue.pda.utils.ACacheHelper;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_wholesaler.database.logic.ShelveService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.Calendar;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * Created by Nat on 2015/5/11.
 */
public class AppHelper {
    /**
     * 清空注册用户账户数据
     * */
    public static void resetMemberAccountData(){
        try{
            MfhLoginService.get().clear();
            ServiceFactory.cleanService();
//            UserProfileHelper.cleanUserProfile();//清空个人信息
        }
        catch(Exception e){
            ZLogger.e(e.toString());
        }
    }

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

}
