package com.manfenjiayuan.pda_supermarket.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.bingshanguxue.pda.bizz.ARCode;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.logic.CashierShopcartService;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.Constants;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.base.InitActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {

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
    public void doAsyncTask() {
        if (!requestPermissions()){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_ANDROID_SETTINGS: {
                if (data != null){
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                if (resultCode == Activity.RESULT_OK) {
                    doAsyncTask();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 权限申请
     * */
    private boolean requestPermissions(){
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
        for (String permission : expectPermissions){
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ZLogger.d(getString(R.string.permission_not_granted, permission));
                lackPermissions.add(permission);
            }
        }

        int size = lackPermissions.size();
        if (size > 0){
            String[] permissions = new String[size];
            for (int i = 0; i < size; i++){
                permissions[i] = lackPermissions.get(i);
            }
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    permissions, Constants.REQUEST_CODE_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            ZLogger.i("Received response for permissions request.");

            // Check if the only required permission has been granted
            boolean isGrannted = PermissionUtil.verifyPermissions(grantResults);
            ZLogger.d("isGrannted=" + isGrannted);
            if (isGrannted){
                doAsyncTask();
            }
            else{
//                MANAGE_APP_PERMISSIONS
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
//                startActivity(intent);
                startActivityForResult(intent, ARCode.ARC_ANDROID_SETTINGS);
            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
