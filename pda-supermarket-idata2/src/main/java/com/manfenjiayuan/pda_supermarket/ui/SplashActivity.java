package com.manfenjiayuan.pda_supermarket.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.skinloader.listener.ILoaderListener;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.manfenjiayuan.business.hostserver.TenantInfoWrapper;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.cashier.database.service.CashierShopcartService;
import com.manfenjiayuan.pda_supermarket.service.DemoIntentService;
import com.manfenjiayuan.pda_supermarket.service.DemoPushService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.uikit.base.ResultCode;
import com.mfh.framework.uikit.widget.LoadingImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;


/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {
    @BindView(R.id.loadingImageView)
    LoadingImageView loadingImageView;
    @BindView(R.id.tv_version)
    TextView tvVersion;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash_style02;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZLogger.d("adb 0010");
        loadingImageView.setBackgroundResource(R.drawable.loading_anim);
        loadingImageView.toggle(true);
        AppInfo appInfo = AnalysisAgent.getAppInfo(AppContext.getAppContext());
        if (appInfo != null) {
            tvVersion.setText(String.format("%s-%d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }

        requestPermissions();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        setupGetui();
    }


    @Override
    protected void initComleted() {
        AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());

        //首次启动(由于应用程序可能会被多次执行在不同的进程中，所以这里在启动页调用)
        ZLogger.df(String.format("应用程序启动(%s-%d）",
                appInfo.getVersionName(), appInfo.getVersionCode()));

        MfhApi.register();
        IMApi.register();

        //首次启动
        if (SharedPrefesManagerFactory.isAppFirstStart()) {
            SharedPrefesManagerFactory.setAppStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(new Date()));
            SharedPrefesManagerFactory.setAppFirstStart(false);
        }

        CashierShopcartService.getInstance().clear();//购物车－收银
        AppHelper.clearRedunantData(false);

        onInitializedCompleted();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ResultCode.ARC_ANDROID_SETTINGS: {
                if (data != null) {
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                doAsyncTask();
            }
            break;
            case ResultCode.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    TenantInfoWrapper hostServer = GlobalInstanceBase.getInstance().getHostServer();
                    if (hostServer == null) {
                        finish();
                    }

                    loadSkin(GlobalInstanceBase.getInstance().getSkinName());
//                    AppIconManager.changeIcon(SplashActivity.this, hostServer.getActivityAlias());

                    MfhLoginService.get().clear();
                    redirectToLogin();
                } else {
                    finish();
                }
            }
            break;
            case ResultCode.ARC_NATIVE_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirectToMain(false);
                } else {
                    finish();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected ArrayList<String> getRequestPermissions() {
        ArrayList<String> expectPermissions = new ArrayList<>();

        expectPermissions.add(Manifest.permission.CAMERA);
        expectPermissions.add(Manifest.permission.READ_CONTACTS);
        //Location:位置服务
        expectPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        expectPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        //MicroPhone:录音
        expectPermissions.add(Manifest.permission.RECORD_AUDIO);
        //Phone:拨打电话
        expectPermissions.add(Manifest.permission.CALL_PHONE);
        expectPermissions.add(Manifest.permission.READ_PHONE_STATE);
        //SMS:短信
        expectPermissions.add(Manifest.permission.RECEIVE_SMS);
        expectPermissions.add(Manifest.permission.READ_SMS);
        //Storage:文件存储
        expectPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        expectPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return expectPermissions;
    }


    @Override
    protected void onPermissionsGranted() {
        super.onPermissionsGranted();
        doAsyncTask();

    }


    /**
     * 设置个推
     */
    private void setupGetui() {
        ZLogger.df("准备初始化个推服务...");
        PushManager.getInstance().initialize(this.getApplicationContext(), DemoPushService.class);

        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), DemoIntentService.class);

        // 检查 so 是否存在
        File file = new File(this.getApplicationInfo().nativeLibraryDir + File.separator + "libgetuiext2.so");
        ZLogger.df("libgetuiext2.so exist = " + file.exists());

        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());
        ZLogger.df("当前应用的cid = " + cid);
    }

    /**
     * 初始化完成
     */
    private void onInitializedCompleted() {
        ZLogger.df("应用程序初始化完成。");

        TenantInfoWrapper hostServer = GlobalInstanceBase.getInstance().getHostServer();
        if (hostServer == null) {
            redirect2HostServer();
            return;
        }

        ZLogger.df("进入租户\n" + JSON.toJSONString(hostServer));
//        MfhApi.URL_BASE_SERVER = String.format("http://%s/pmc", hostServer.getDomainUrl());
//        MobileApi.DOMAIN = hostServer.getDomainUrl();
////        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
//        MfhApi.register();
//        IMApi.register();

        if (MfhLoginService.get().haveLogined()) {
            redirectToMain(true);
        } else {
            redirectToLogin();
        }
    }

    private void redirect2HostServer() {
        ZLogger.df("准备跳转到域名选择页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putBoolean(BaseActivity.EXTRA_KEY_FULLSCREEN, true);
        extras.putInt(RouteActivity.EXTRA_KEY_FRAGMENT_TYPE, RouteActivity.FT_APP_HOSTSERVER);
        extras.putInt(HostServerFragment.EXTRA_KEY_MODE, 1);

        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtras(extras);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, ResultCode.ARC_APP_HOSTSERVER);
    }

    /**
     * 跳转至登录
     */
    private void redirectToLogin() {
        ZLogger.df("准备跳转到登录页");

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ResultCode.ARC_NATIVE_SIGNIN);
    }

    /**
     * 初始化完成,跳转至首页
     */
    private void redirectToMain(boolean delayed) {
        ZLogger.df("准备跳转到首页");
        if (delayed) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.actionStart(SplashActivity.this, null);
                    finish();
                }
            }, 500);
        } else {
            MainActivity.actionStart(SplashActivity.this, null);
            finish();
        }
    }

    /**
     * 加载皮肤
     * */
    private void loadSkin(String skinName) {
        SkinManager.getInstance().loadSkin(skinName,
                new ILoaderListener() {
                    @Override
                    public void onStart() {
                        ZLogger.df("正在切换主题");
//                        dialog.show();
                    }

                    @Override
                    public void onSuccess() {
                        ZLogger.df("切换主题成功");
                        DialogUtil.showHint("切换租户成功");
                    }

                    @Override
                    public void onFailed(String errMsg) {
                        ZLogger.df("切换主题失败:" + errMsg);
                        DialogUtil.showHint(errMsg);
                    }

                    @Override
                    public void onProgress(int progress) {
                        ZLogger.d("主题皮肤文件下载中:" + progress);
                    }
                }

        );

    }
}
