package com.manfenjiayuan.pda_supermarket.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.logic.CashierShopcartService;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.InitActivity;

import butterknife.Bind;

/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {

    private static final int REQUEST_PERMISSION = 0;

    @Bind(R.id.tv_version)
    TextView tvVersion;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZLogger.d("adb 0010");

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        PackageManager pkgManager = getPackageManager();
        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
        boolean sdCardWritePermission =
                pkgManager.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
        // read phone state用于获取 imei 设备信息
        boolean phoneSatePermission =
                pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
            requestPermission();
        } else {
            // SDK初始化，第三方程序启动时，都要进行SDK初始化工作
            setupGetui();
        }

        AppInfo appInfo = AnalysisAgent.getAppInfo(AppContext.getAppContext());
        if (appInfo != null){
            tvVersion.setText(String.format("%s-%d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {
        //首次启动
        if(SharedPreferencesManager.isAppFirstStart()){
            //清空旧缓存
//            AppHelper.clearAppCache();
            SharedPreferencesManager.setTerminalId("");
            SharedPreferencesManager.setAppFirstStart(false);
        }

        CashierShopcartService.getInstance().clear();//购物车－收银
        AppHelper.clearRedunantData(false);

        onInitializedCompleted();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                PushManager.getInstance().initialize(this.getApplicationContext());
            } else {
                Log.e("GetuiSdkDemo",
                        "we highly recommend that you need to grant the special permissions before initializing the SDK, otherwise some "
                                + "functions will not work");
                setupGetui();
            }
        } else {
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 设置个推
     * */
    private void setupGetui(){
        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());
        ZLogger.df(String.format("准备初始化个推服务(%s)", cid));
        PushManager.getInstance().initialize(this.getApplicationContext());
    }

    /**
     *  初始化完成
     */
    private void onInitializedCompleted(){
        ZLogger.d("应用程序初始化完成。");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.actionStart(SplashActivity.this, null);
                finish();
            }
        }, 500);
    }
}
