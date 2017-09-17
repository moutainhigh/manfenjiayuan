package com.mfh.enjoycity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMClient;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.events.WxPayEvent;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.enjoycity.utils.UserProfileHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.framework.prefs.SharedPrefesBase;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.login.logic.MfhLoginService;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 应用程序帮助类
 * Created by Shicy on 14-3-18.
 */
public class AppHelper {

    public static boolean IMAGE_LOAD_MOD_UINIVERSAL = true;

    /**
     * 清空注册用户账户数据
     * */
    public static void resetMemberAccountData(){
        try{
            //old,在这里执行clear()方法，如果退出失败（比如网络断开），此时数据已经清空，但是也没还没有变化。
//            IMConversationService.get().clearMsgs();
//            AnonymousAddressService.get().clear();
            //清空小伙伴信息
            SharedPrefesBase.set(AppContext.getAppContext(), Constants.PREF_NAME_APP_BIZ, Constants.PREF_KEY_PARTER_COUNT, 0);
            //清空购物车，订单信息
            ShoppingCartService.get().clear();
            ShopcartHelper.getInstance().reset();

            MfhLoginService.get().clear();
            ServiceFactory.cleanService();
            UserProfileHelper.cleanUserProfile();//清空个人信息
        }
        catch(Exception e){
            ZLogger.e(e.toString());
        }
    }

    /**
     * 清空匿名用户账户数据
     * */
    public static void resetAnonymousAccountData(){

    }

    /**
     * 广播微信支付结果
     * */
    public static void broadcastWXPayResp(int errCode, String errStr){
        Bundle extras = new Bundle();
        extras.putInt(Constants.BROADCAST_KEY_WXPAY_RESP_ERRCODE, errCode);
        extras.putString(Constants.BROADCAST_KEY_WXPAY_RESP_ERRSTR, errStr);

//        if(HybridActivity.getInstance() != null){
//            HybridActivity.getInstance().parseWxpayResp(extras);
//        }
//        MfhApplication.getAppContext().sendBroadcast(new Intent(Constants.BROADCAST_ACTION_WXPAY_RESP));

//        No subscribers registered for event class com.mfh.enjoycity.events.WxPayEvent
        EventBus.getDefault().post(
                new WxPayEvent(errCode, errStr));
    }

    /**
     * 手机有menu键actionbar就不会显示3个点的更多或者说3个点的menu按钮，调用该方法让他显示
     * @param context
     */
    public static void overflowMenu(Context context) {
        try {
            ViewConfiguration config = ViewConfiguration.get(context);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleSoftInput(final Context context) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }, 400);
    }

    public static void hideSoftInput(Activity activity) {
        View currentFocusView = activity.getCurrentFocus();
        if (currentFocusView == null)
            return ;

        IBinder windowToken = currentFocusView.getWindowToken();
        if (windowToken == null)
            return ;

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideSoftInputEver(Activity activity) {
        View currentFocusView = activity.getCurrentFocus();

        hideSoftInputEver(activity, currentFocusView);
    }

    public static void hideSoftInputEver(Activity activity, View view) {
        if (view == null)
            return ;

        IBinder windowToken = view.getWindowToken();
        if (windowToken == null)
            return ;

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }


    public static void startSMSMessage(Activity activity, String number, String message) {
        Uri uri = Uri.parse("smsto:" + number);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.putExtra("sms_body", message);
        activity.startActivity(intent);
    }

    /**
     * 保存用户登录数据
     * */
    public static void saveUserLoginInfo(String data){
        ZLogger.d("saveUserLoginInfo.data = " + data);
        JSONObject jsonObject = JSON.parseObject(data);
        String uid = jsonObject.getString("uid");
        String pwd = jsonObject.getString("pwd");
        JSONObject result = jsonObject.getJSONObject("result");
        if(result != null){
            //解析并保存用户登录信息
            UserMixInfo userMixInfo = JSONObject.parseObject(result.toJSONString(), UserMixInfo.class);
            MfhLoginService.get().saveUserMixInfo(uid, pwd, userMixInfo);

            IMClient.getInstance().registerBridge();
        }
    }

    private static AssetManager am;
    /**
     * 获取asset资源管理器
     * @return
     * @author zhangyz created on 2013-5-25
     */
    public static AssetManager getAm() {
        if (am == null) {
            Context context = MfhApplication.getAppContext();
            if (context == null)
                throw new RuntimeException("请在AndroidManifest.xml配置文件中使用android:name=\"com.mfh.comna.bizz.ComnApplication\"");
            am = context.getApplicationContext().getAssets();
        }
        return am;
    }

    /**
     * 清除APP缓存
     * */
    public static void clearAppCache(){
        DataCleanManager.cleanDatabases(MfhApplication.getAppContext());
        //清除数据缓存
        DataCleanManager.cleanInternalCache(MfhApplication.getAppContext());

        //清除编辑器保存的临时内容Properties
        //清除webview缓存
//        WebViewUtils.clearCacheFolder();

        //清除图片缓存
//        Glide.get(MfhApplication.getAppContext()).clearMemory();
    }

    public static void AppExit(){
        // 保存统计数据
//        MobclickAgent.onKillProcess(MfhApplication.getAppContext());

        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
