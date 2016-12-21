package com.manfenjiayuan.pda_supermarket;


import android.os.Environment;

import com.bingshanguxue.skinloader.config.SkinConfig;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.bingshanguxue.skinloader.utils.SkinFileUtils;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import java.io.File;
import java.io.IOException;

/**
 * Created by NAT.ZZN(bingshanguxue) on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    @Override
    protected boolean isReleaseVersion() {
        //TODO,支持配置开发服务器&正式服务器，需要重新启动
//        return false;
        return SharedPrefesManagerFactory.isReleaseVersion();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        int pid = android.os.Process.myPid();
        String processAppName = getProcessName(this, pid);
        ZLogger.df("进程:" + processAppName);
        // 如果app启用了远程的service，此application:onCreate会被调用2次
        // 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
        if (processAppName != null && processAppName.equalsIgnoreCase(getPackageName())) {
            configBugly();

            if (BizConfig.RELEASE){
//            ZLogger.d("正式版本");
                ZLogger.LOG_ENABLED = true;
            }
            else{
//            ZLogger.d("测试版本");
                ZLogger.LOG_ENABLED = true;
            }
            //初始化IM模块
            IMClient.getInstance().init(getApplicationContext());

            initSkinLoader();

            debugPrint();
        }

        ZLogger.df(String.format("initialize finished(%s)", processAppName));

//        //注册应用id到微信
//        WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false).registerApp(WXConstants.APP_ID);
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        try {
            String[] skinFiles = getAssets().list(SkinConfig.SKIN_DIR_NAME);
            for (String fileName : skinFiles) {
                File file = new File(SkinFileUtils.getSkinDir(this), fileName);
                if (!file.exists()){
                    ZLogger.d("拷贝皮肤文件:" + fileName);
                    SkinFileUtils.copySkinAssetsToDir(this, fileName, SkinFileUtils.getSkinDir(this));
                }
                else{
                    ZLogger.d("已经安装过皮肤文件:" + fileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
        SkinManager.getInstance().init(this);
        SkinManager.getInstance().loadSkin();
    }

    private void configBugly(){
        /***** Beta高级设置 *****/
        /**
         * true表示app启动自动初始化升级模块; false不会自动初始化;
         * 开发者如果担心sdk初始化影响app启动速度，可以设置为false，
         * 在后面某个时刻手动调用Beta.init(getApplicationContext(),false);
         */
        Beta.autoInit = true;
        /**
         * true表示初始化时自动检查升级; false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法;
         */
        Beta.autoCheckUpgrade = true;
        /**
         * 设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略);
         */
        Beta.upgradeCheckPeriod = 60 * 1000;
        /**
         * 设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
         */
        Beta.initDelay = 1000;
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

        //Bugly SDK初始化
        Bugly.init(getApplicationContext(), "900029972", false);
    }

}
