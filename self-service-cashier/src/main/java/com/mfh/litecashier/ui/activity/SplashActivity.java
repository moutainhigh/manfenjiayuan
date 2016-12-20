package com.mfh.litecashier.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.bingshanguxue.skinloader.listener.ILoaderListener;
import com.bingshanguxue.skinloader.loader.SkinManager;
import com.igexin.sdk.PushManager;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.manfenjiayuan.business.GlobalInstanceBase;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.mfh.framework.uikit.base.ResultCode;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.im.IMClient;
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
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.uikit.widget.LoadingImageView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.service.DemoIntentService;
import com.mfh.litecashier.service.DemoPushService;
import com.mfh.litecashier.utils.AppHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 开屏页
 * <ol>
 * <li>初始化数据库</li>
 * <li>初始化个推服务</li>
 * </ol>
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
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZLogger.d("adb 000021");
        loadingImageView.setBackgroundResource(R.drawable.loading_anim);
        loadingImageView.toggle(true);

        AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());
        if (appInfo != null) {
            tvVersion.setText(String.format("%s-%d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        } else {
            tvVersion.setText("");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        ZLogger.df("set database version.");
        DbVersion.setDomainVersion("LITECASHIER.CLIENT.DB.UPGRADE", 17);

//        String hostServerData = SharedPrefesManagerBase.getText(SharedPrefesManagerBase.PK_S_HOSTSERVER, null);
//        HostServer hostServer = JSONObject.toJavaObject(JSONObject.parseObject(hostServerData),
//                HostServer.class);
//        if (hostServer == null){
//            hostServer = new HostServer(1L,
//                    "米西厨房", "admin.mixicook.com",
//                    "http://admin.mixicook.com/pmc",
//                    "http://mobile.mixicook.com/mfhmobile/mobile/api",
//                    R.mipmap.ic_textlogo_mixicook, R.mipmap.ic_hybridlogo_mixicook,
//                    AppIconManager.ACTIVITY_ALIAS_CASHIER_MIXICOOK, "mixicook.skin");
//            SharedPrefesManagerBase.set(SharedPrefesManagerBase.PK_S_HOSTSERVER,
//                    JSONObject.toJSONString(hostServer));
//        }
    }

    @Override
    protected void initSecondary() {
        super.initSecondary();
    }

    @Override
    public void doAsyncTask() {
        if (!requestPermissions()) {
            return;
        }

        setupGetui();

        super.doAsyncTask();
    }

    @Override
    protected void initComleted() {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                AppHelper.saveAppStartupDatetime();
                AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());

                //首次启动(由于应用程序可能会被多次执行在不同的进程中，所以这里在启动页调用)
                ZLogger.df(String.format("应用程序启动(%s-%d）",
                        appInfo.getVersionName(), appInfo.getVersionCode()));

                MfhApi.register();
                IMApi.register();

                if (SharedPrefesManagerFactory.isAppFirstStart()) {
                    //保存应用启动时间
                    SharedPrefesManagerFactory.setAppStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(new Date()));
                    SharedPrefesManagerFactory.setAppFirstStart(false);
                }

                CashierShopcartService.getInstance().clear();//购物车－收银
                PosCategoryGodosTempService.getInstance().clear();

                AppHelper.clearRedunantData(false);

                //加载会话和群组
                IMClient.getInstance().groupManager().loadAllGroups();
                IMClient.getInstance().chatManager().loadAllConversations();

                //验证登录状态是否有效
                if (MfhLoginService.get().haveLogined()) {
                    subscriber.onNext(true);
                } else {
                    MfhLoginService.get().clear();
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
                        if (hostServer == null) {
                            redirect2HostServer();
                            return;
                        }

                        ZLogger.df("进入租户\n" + JSON.toJSONString(hostServer));
//                        MfhApi.URL_BASE_SERVER = String.format("http://%s/pmc", hostServer.getDomainUrl());
//                        MobileApi.DOMAIN = hostServer.getDomainUrl();
////                        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
//                        MfhApi.register();
//                        IMApi.register();

                        if (aBoolean) {
                            redirectToMain(true);
                        } else {
                            redirectToLogin();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ResultCode.ARC_ANDROID_SETTINGS: {
                if (data != null){
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                doAsyncTask();
            }
            break;
            case ResultCode.ARC_NATIVE_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirectToMain(false);
                }
            }
            break;
            case ResultCode.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    HostServer hostServer = GlobalInstanceBase.getInstance().getHostServer();
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 个推SDK初始化
     * 我们建议应用开发者在Activity或Service类中调用个推SDK的初始化方法，确保SDK在各种情况下都能正常运行。
     * 一般情况下可以在主Activity的onCreate()或者onResume()方法中调用，也可以在多个主要界面Activity的
     * onCreate()或onResume()方法中调用。反复调用SDK初始化并不会有什么副作用。
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

        String cid = PushManager.getInstance().getClientid(CashierApp.getAppContext());
        ZLogger.df("当前应用的cid = " + cid);
    }

    @Override
    protected ArrayList<String> getRequestPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        //Camera
        permissionsNeeded.add(Manifest.permission.CAMERA);
        //Contact
        permissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        //Location:位置服务
        permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        expectPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        //MicroPhone:录音
        permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        //Phone:拨打电话
        permissionsNeeded.add(Manifest.permission.CALL_PHONE);
//        expectPermissions.add(Manifest.permission.READ_PHONE_STATE);
        //SMS:短信
        permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
//        expectPermissions.add(Manifest.permission.READ_SMS);
        //Storage:文件存储
        permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        expectPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionsNeeded;
    }

    @Override
    protected void onPermissionsGranted() {
        super.onPermissionsGranted();
        doAsyncTask();
    }


    private void redirect2HostServer() {
        ZLogger.d("准备跳转到域名选择页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putBoolean(BaseActivity.EXTRA_KEY_FULLSCREEN, true);
        extras.putInt(RouteActivity.EXTRA_KEY_FRAGMENT_TYPE, RouteActivity.FT_APP_HOSTSERVER);
        extras.putInt(HostServerFragment.EXTRA_KEY_MODE, 0);

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
        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
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
