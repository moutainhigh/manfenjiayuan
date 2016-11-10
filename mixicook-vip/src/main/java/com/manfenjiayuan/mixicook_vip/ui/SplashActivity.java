package com.manfenjiayuan.mixicook_vip.ui;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.utils.AppHelper;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.Constants;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
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

    public static PendingIntent generatePendingIntent(Context context){
        Intent intent =new Intent(context, SplashActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FILL_IN_DATA);
        intent.setAction(String.valueOf(System.currentTimeMillis()));

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        AppHelper.getInstance().addActivity(this);

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
//        ZLogger.df("initializing getui sdk...");
//        PushManager.getInstance().initialize(AppContext.getAppContext());
//        PushManager.getInstance().stopService(this);

        AppInfo appInfo = AnalysisAgent.getAppInfo(AppContext.getAppContext());
        if (appInfo != null){
            tvVersion.setText(String.format("%s-%d", appInfo.getVersionName(),
                    appInfo.getVersionCode()));
        }
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
    public void initPrimary() {
        super.initPrimary();

//        申请权限

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);
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
    protected void initComleted() {
        //首次启动
        if(SharedPreferencesManager.isAppFirstStart()){
            SharedPreferencesManager.setTerminalId("");
            SharedPreferencesManager.setAppFirstStart(false);
        }

//        AppHelper.clearOldPosOrder(15);

        if (MfhLoginService.get().haveLogined()) {
            redirect2Main();
        } else {
            redirect2Login();
        }
    }

    /**
     *  初始化完成
     */
    private void redirect2Main(){
        ZLogger.d("应用程序初始化完成。");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIHelper.startActivity(SplashActivity.this, MainActivity.class);
                finish();
            }
        }, 500);
    }

    /** 跳转至登录页面
    */
    private void redirect2Login() {
        ZLogger.d("初始化应用，未登录准备跳转到登录页面");
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);

        if (AppContext.loginType == 0) {
            Intent intent = new Intent(this, SignInActivity.class);
            intent.putExtras(extras);
            startActivityForResult(intent, ARCode.ARC_SIGNIN);
        } else {
            Intent intent = new Intent(this, SmsSignActivity.class);
            intent.putExtras(extras);
            startActivityForResult(intent, ARCode.ARC_SIGNIN);
        }
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
            case ARCode.ARC_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirect2Main();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

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


}
