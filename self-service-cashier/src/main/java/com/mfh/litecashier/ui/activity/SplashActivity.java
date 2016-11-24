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

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.manfenjiayuan.business.route.Route;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.mobile.MobileApi;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.uikit.widget.LoadingImageView;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.utils.AppHelper;

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

        ZLogger.d("adb 000019");
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
        DbVersion.setDomainVersion("LITECASHIER.CLIENT.DB.UPGRADE", 16);

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

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
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
                        HostServer hostServer = SharedPrefesManagerBase.getHostServer();
                        if (hostServer == null) {
                            redirect2HostServer();
                            return;
                        }

                        ZLogger.d("进入租户\n" + JSON.toJSONString(hostServer));
                        MfhApi.URL_BASE_SERVER = hostServer.getBaseServerUrl();
                        MobileApi.DOMAIN = hostServer.getHost();
//                        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
                        MfhApi.register();
                        IMApi.register();

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
            case Route.ARC_ANDROID_SETTINGS: {
                if (data != null){
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                doAsyncTask();
            }
            break;
            case Constants.ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirectToMain(false);
                }
            }
            break;
            case Route.ARC_APP_HOSTSERVER: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    HostServer hostServer = (HostServer) data.getSerializableExtra(HostServerFragment.EXTRA_KEY_HOSTSERVER);
                    if (hostServer == null) {
                        finish();
                    }

//                    AppIconManager.changeIcon(SplashActivity.this, hostServer.getActivityAlias());

                    SharedPrefesManagerBase.setHostServer(hostServer);
                    MfhApi.URL_BASE_SERVER = hostServer.getBaseServerUrl();
                    MobileApi.DOMAIN = hostServer.getHost();
//                    IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
                    MfhApi.register();
                    IMApi.register();

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
     * 设置个推
     */
    private void setupGetui() {
        String cid = PushManager.getInstance().getClientid(CashierApp.getAppContext());
        ZLogger.df(String.format("准备初始化个推服务(%s)", cid));
        PushManager.getInstance().initialize(this.getApplicationContext());
    }

    /**
     * 权限申请
     */
    private boolean requestPermissions() {
        ArrayList<String> expectPermissions = new ArrayList<>();
        //Constant
        expectPermissions.add(Manifest.permission.CAMERA);
        expectPermissions.add(Manifest.permission.READ_CONTACTS);
        //Location
        expectPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        expectPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        //MicroPhone:录音
        expectPermissions.add(Manifest.permission.RECORD_AUDIO);
        //Phone:拨打电话
        expectPermissions.add(Manifest.permission.CALL_PHONE);
        expectPermissions.add(Manifest.permission.READ_PHONE_STATE);
        //SMS:短信
        expectPermissions.add(Manifest.permission.RECEIVE_SMS);
        //Storage:文件存储
        expectPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        expectPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);


        List<String> lackPermissions = new ArrayList<>();
        for (String permission : expectPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ZLogger.d(getString(R.string.permission_not_granted, permission));
                lackPermissions.add(permission);
            }
        }

        int size = lackPermissions.size();
        if (size > 0) {
            String[] permissions = new String[size];
            for (int i = 0; i < size; i++) {
                permissions[i] = lackPermissions.get(i);
            }
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, permissions, Route.ARC_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Route.ARC_PERMISSIONS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            ZLogger.i("Received response for permissions request.");

            // Check if the only required permission has been granted
            boolean isGrannted = PermissionUtil.verifyPermissions(grantResults);
            ZLogger.d("isGrannted=" + isGrannted);
            if (isGrannted) {
                doAsyncTask();
            } else {
//                MANAGE_APP_PERMISSIONS

                showConfirmDialog("应用需要相关权限才能正常使用，请在设置中开启",
                        "立刻开启", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();


                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
//                startActivity(intent);
                                startActivityForResult(intent, Route.ARC_ANDROID_SETTINGS);
                            }
                        }, "残忍拒绝", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        startActivityForResult(intent, Route.ARC_APP_HOSTSERVER);
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
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
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
}
