package com.manfenjiayuan.cashierdisplay;

import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by bingshanguxue on 16/3/24.
 */
public class AppContext extends MfhApplication {
    @Override
    protected boolean isReleaseVersion() {
//        return SharedPreferencesManager.isReleaseVersion();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**初始化Bugly*/
//        CrashReport.initCrashReport(getApplicationContext(), "900023453", false);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        //...在这里设置strategy的属性,在bugly初始化时传入
        strategy.setAppChannel("满分家园"); //设置渠道
         strategy.setAppVersion("0.0.1"); //App的版本
         strategy.setAppPackageName("com.manfenjiayuan.cashierdisplay"); //App的包名

        CrashReport.initCrashReport(getApplicationContext(), "900023453", true, strategy);

        if (BizConfig.RELEASE){
//            ZLogger.d("正式版本");
            ZLogger.LOG_ENABLED = true;
//            SharedPreferencesHelper.PREF_NAME_PREFIX = SharedPreferencesHelper.RELEASE_PREFIX;
//            Constants.CACHE_NAME = "ACache_Release";
//            DebugHelper.debug();
        }
        else{
//            ZLogger.d("测试版本");
            ZLogger.LOG_ENABLED = true;
//            SharedPreferencesHelper.PREF_NAME_PREFIX = SharedPreferencesHelper.DEV_PREFIX;
//            Constants.CACHE_NAME = "ACache_Dev";
//          DebugHelper.debug();
        }

//            DebugHelper.debug();

//        //注册应用id到微信
//        WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false).registerApp(WXConstants.APP_ID);
//

        int pid = android.os.Process.myPid();
        String processAppName = getProcessName(this, pid);
        ZLogger.d("processAppName=" + processAppName);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {
            //初始化IM模块
            IMClient.getInstance().init(getApplicationContext());
        }

        //测试Bugly
//        CrashReport.testJavaCrash();

        ZLogger.df(String.format("Application--initialize finished(%s)", processAppName));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //do release operation
        ZLogger.d("onLowMemory");

//        AppHelper.clearCache();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ZLogger.d("onTrimMemory:" + level);

//        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
//            AppHelper.clearCache();
//        }
    }

    @Override public void onTerminate() {
        super.onTerminate();
    }
}
