package com.mfh.owner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.IBinder;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataCleanManager;
import com.mfh.framework.login.entity.UserMixInfo;
import com.mfh.framework.login.logic.MfhLoginService;
import com.umeng.analytics.MobclickAgent;

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
     * 清空匿名用户账户数据
     * */
    public static void resetAnonymousAccountData(){

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

//    public static void callTel(Activity activity, String number) {
//        Uri uri = Uri.parse("tel:" + number);
//        Intent intent = new Intent(Intent.ACTION_CALL, uri);
//        activity.startActivity(intent);
//    }

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
    }

    public static void AppExit(){
        // 保存统计数据
        MobclickAgent.onKillProcess(MfhApplication.getAppContext());

        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
