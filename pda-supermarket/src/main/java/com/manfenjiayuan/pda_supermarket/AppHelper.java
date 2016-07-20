package com.manfenjiayuan.pda_supermarket;


import com.manfenjiayuan.pda_supermarket.database.logic.ShelveService;
import com.manfenjiayuan.pda_supermarket.database.logic.StockTakeService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.Calendar;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * Created by Nat on 2015/5/11.
 */
public class AppHelper {

    public static boolean IMAGE_LOAD_MOD_UINIVERSAL = true;

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
     * 清除旧数据
     *
     * @param saveDate 保存的天数
     */
    public static void clearOldPosOrder(int saveDate) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0 - saveDate);//
        String expireCursor = TimeCursor.InnerFormat.format(calendar.getTime());
        ZLogger.d(String.format("Initialize--订单过期时间(%s)保留最近30天数据。", expireCursor));

        //清除订单
        StockTakeService.get().deleteBy(String.format("updatedDate < '%s'", expireCursor));
        ShelveService.get().deleteBy(String.format("updatedDate < '%s'", expireCursor));
    }

}
