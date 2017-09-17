package com.mfh.litecashier;

import android.content.ComponentCallbacks2;
import android.os.Environment;

import com.bingshanguxue.skinloader.config.SkinConfig;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.bingshanguxue.skinloader.utils.SkinFileUtils;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import java.io.File;
import java.io.IOException;


/**
 * Created by bingshanguxue on 2015/7/10.
 */
public class CashierApp extends MfhApplication {
    private RefWatcher mRefWatcher;

    @Override
    protected boolean isReleaseVersion() {
        return false;
//        return SharedPrefesManagerFactory.isReleaseVersion();
    }

    @Override
    public void onCreate() {
        initIflytek();

        super.onCreate();

//        byte[] test = EmbPrinter.setFont(0, 1, 1, 0, 0);
//        ZLogger.d(Base64.encodeToString(test, Base64.DEFAULT));
//        EscCommand esc = new EscCommand();
//        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF,
//                EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);

        SMScaleSyncManager2.FOLDER_PATH_SMSCALE = getPackageName() + File.separator + "smscale";

        mRefWatcher = LeakCanary.install(this);

//        //注册应用id到微信
//        WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false).registerApp(WXConstants.APP_ID);

        int pid = android.os.Process.myPid();
        String processAppName = getProcessName(this, pid);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {
            configBugly();

            if (BizConfig.RELEASE) {
//            ZLogger.d("正式版本");
                SharedPreferencesUltimate.PREF_NAME_PREFIX = SharedPreferencesUltimate.RELEASE_PREFIX;
//            Constants.CACHE_NAME = "ACache_Release";
            } else {
//            ZLogger.d("测试版本");
                SharedPreferencesUltimate.PREF_NAME_PREFIX = SharedPreferencesUltimate.DEV_PREFIX;
                ACacheHelper.CACHE_NAME = "ACache_Dev";

                debugPrint();
            }
//            ZLogger.LOG_ENABLED = isReleaseVersion();//SharedPrefesManagerFactory.isSuperPermissionGranted();

            //初始化IM模块
            IMClient.getInstance().init(getApplicationContext());

            initSkinLoader();
        }


        ZLogger.d(String.format("initialize finished(%s)", processAppName));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //do release operation
        ZLogger.d("onLowMemory");

        AppHelper.clearCacheData();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ZLogger.d("onTrimMemory:" + level);

        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            AppHelper.clearCacheData();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * Beta高级设置
     */
    private void configBugly() {
        ZLogger.d("Beta高级设置...");
        /***** Beta高级设置 *****/
        /**
         * true表示app启动自动初始化升级模块;
         * false不会自动初始化;
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false，
         * 在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
         */
        Beta.autoInit = true;
        /**
         * true表示初始化时自动检查升级;
         * false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
         */
        Beta.autoCheckUpgrade = true;
        /**
         * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
         */
        Beta.upgradeCheckPeriod = 60 * 1000;
        /**
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
         */
        Beta.initDelay = 2 * 1000;
        /**
         * 设置通知栏大图标，largeIconId为项目中的图片资源;
         */
        Beta.largeIconId = R.mipmap.ic_launcher;
        /**
         * 设置状态栏小图标，smallIconId为项目中的图片资源Id;
         */
        Beta.smallIconId = R.mipmap.ic_launcher;/**
         * 设置更新弹窗默认展示的banner，defaultBannerId为项目中的图片资源Id;
         * 当后台配置的banner拉取失败时显示此banner，默认不设置则展示“loading“;
         */
        Beta.defaultBannerId = R.mipmap.ic_launcher;
        /**
         * 设置sd卡的Download为更新资源保存目录;
         * 后续更新资源会保存在此目录，需要在manifest中添加WRITE_EXTERNAL_STORAGE权限;
         */
        Beta.storageDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /**
         * 已经确认过的弹窗在APP下次启动自动检查更新时会再次显示;
         */
        Beta.showInterruptedStrategy = true;
        /**
         * 只允许在MainActivity上显示更新弹窗，其他activity上不显示弹窗; 不设置会默认所有activity都可以显示弹窗;
         */
//        Beta.canShowUpgradeActs.add(MainActivity.class);

        /***** Bugly高级设置 *****/
//        BuglyStrategy strategy = new BuglyStrategy();
        /**
         * 设置app渠道号
         */
//        strategy.setAppChannel(APP_CHANNEL);

        /***** 统一初始化Bugly产品，包含Beta *****/
//        Bugly.init(this, APP_ID, true, strategy);
        /**
         * 已经接入Bugly用户改用上面的初始化方法,不影响原有的crash上报功能;
         * init方法会自动检测更新，不需要再手动调用Beta.checkUpdate(),如需增加自动检查时机可以使用Beta.checkUpdate(false,false);
         * 参数1： applicationContext
         * 参数2：appId
         * 参数3：是否开启debug
         */
        Bugly.init(getApplicationContext(), "900030108", false);
    }


    /**
     * 初始化科大讯飞MSC
     * <p/>
     * 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
     * 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
     * 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，
     * 请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
     * 参数间使用半角“,”分隔。
     * 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
     * <p/>
     * 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
     */
    private void initIflytek() {
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=57982371");
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        try {
            String[] skinFiles = getAssets().list(SkinConfig.SKIN_DIR_NAME);
            for (String fileName : skinFiles) {
                File file = new File(SkinFileUtils.getSkinDir(this), fileName);
                if (!file.exists())
                    SkinFileUtils.copySkinAssetsToDir(this, fileName, SkinFileUtils.getSkinDir(this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkinManager.getInstance().init(this);
        SkinManager.getInstance().loadSkin();
    }

}
