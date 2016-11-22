package com.manfenjiayuan.pda_supermarket.ui;

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
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.hostserver.HostServer;
import com.manfenjiayuan.business.hostserver.HostServerFragment;
import com.manfenjiayuan.business.route.Route;
import com.manfenjiayuan.business.route.RouteActivity;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.business.utils.SharedPrefesManagerBase;
import com.manfenjiayuan.im.IMApi;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.logic.CashierShopcartService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.upgrade.DbVersion;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;

/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {
    @Bind(R.id.loadingImageView)
    LoadingImageView loadingImageView;
    @Bind(R.id.tv_version)
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
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);

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
    public void doAsyncTask() {
        if (!requestPermissions()) {
            return;
        }

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。

        setupGetui();

        super.doAsyncTask();
        // TODO: 07/11/2016
    }

    @Override
    protected void initComleted() {
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
            case Route.ARC_ANDROID_SETTINGS: {
                if (data != null) {
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                if (resultCode == Activity.RESULT_OK) {
                    doAsyncTask();
                }
                else {
                    finish();
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
            case Route.ARC_NATIVE_SIGNIN: {
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

    /**
     * 权限申请
     */
    private boolean requestPermissions() {
        ArrayList<String> expectPermissions = new ArrayList<>();
        expectPermissions.add(Manifest.permission.CAMERA);
        expectPermissions.add(Manifest.permission.READ_CONTACTS);
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
            ActivityCompat.requestPermissions(this,
                    permissions, Route.ARC_PERMISSIONS);
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

    /**
     * 设置个推
     */
    private void setupGetui() {
        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());
        ZLogger.df(String.format("准备初始化个推服务(%s)", cid));
        PushManager.getInstance().initialize(this.getApplicationContext());
    }

    /**
     * 初始化完成
     */
    private void onInitializedCompleted() {
        ZLogger.d("应用程序初始化完成。");
        HostServer hostServer = SharedPrefesManagerBase.getHostServer();
        if (hostServer == null) {
            redirect2HostServer();
            return;
        }

        ZLogger.d("进入租户\n" + JSON.toJSONString(hostServer));
        MfhApi.URL_BASE_SERVER = hostServer.getBaseServerUrl();
        MobileApi.DOMAIN = hostServer.getHost();
//        IMApi.URL_MOBILE_MESSAGE = hostServer.getBaseMessageUrl();
        MfhApi.register();
        IMApi.register();

        if (MfhLoginService.get().haveLogined()) {
            redirectToMain(true);
        } else {
            redirectToLogin();
        }
    }

    private void redirect2HostServer() {
        ZLogger.d("准备跳转到域名选择页面");
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putBoolean(BaseActivity.EXTRA_KEY_FULLSCREEN, true);
        extras.putInt(RouteActivity.EXTRA_KEY_FRAGMENT_TYPE, RouteActivity.FT_APP_HOSTSERVER);
        extras.putInt(HostServerFragment.EXTRA_KEY_MODE, 1);

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
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Route.ARC_NATIVE_SIGNIN);
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
