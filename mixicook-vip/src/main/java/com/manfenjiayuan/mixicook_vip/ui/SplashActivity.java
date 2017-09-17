package com.manfenjiayuan.mixicook_vip.ui;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import com.manfenjiayuan.mixicook_vip.service.DemoIntentService;
import com.mfh.framework.uikit.base.ResultCode;
import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.utils.AppHelper;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.system.PermissionUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;


/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {

    @BindView(R.id.tv_version)
    TextView tvVersion;

    public static PendingIntent generatePendingIntent(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FILL_IN_DATA);
        intent.setAction(String.valueOf(System.currentTimeMillis()));

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash_style01;
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
        if (appInfo != null) {
            tvVersion.setText(String.format("%s-%d", appInfo.getVersionName(),
                    appInfo.getVersionCode()));
        }
    }

    @Override
    public void doAsyncTask() {
        requestPermissions();

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。

        setupGetui();

        super.doAsyncTask();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

//        申请权限

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected ArrayList<String> getRequestPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        permissionsNeeded.add(Manifest.permission.CAMERA);
        permissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        //Location:位置服务
        permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        //MicroPhone:录音
        permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        //Phone:拨打电话
        permissionsNeeded.add(Manifest.permission.CALL_PHONE);
//        permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        //SMS:短信
        permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
//        permissionsNeeded.add(Manifest.permission.READ_SMS);
        //Storage:文件存储
        permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionsNeeded;
    }


    @Override
    protected void initComleted() {
        //首次启动
        if (SharedPrefesManagerFactory.isAppFirstStart()) {
            SharedPrefesManagerFactory.setAppStartupDateTime(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(new Date()));
            SharedPrefesManagerFactory.setAppFirstStart(false);
        }

        if (MfhLoginService.get().haveLogined()) {
            redirect2Main();
        } else {
            redirect2Login();
        }
    }

    /**
     * 初始化完成
     */
    private void redirect2Main() {
        ZLogger.d("应用程序初始化完成。");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIHelper.startActivity(SplashActivity.this, MainActivity.class);
                finish();
            }
        }, 500);
    }

    /**
     * 跳转至登录页面
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
            case ResultCode.ARC_ANDROID_SETTINGS: {
                if (data != null) {
                    ZLogger.d(StringUtils.decodeBundle(data.getExtras()));
                }
                //设置页面返回后，重新检测权限是否开启
                doAsyncTask();
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

    @Override
    protected void onPermissionsGranted() {
        super.onPermissionsGranted();
        doAsyncTask();
    }

    /**
     * 设置个推
     */
    private void setupGetui() {
        String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());
        ZLogger.df(String.format("准备初始化个推服务(%s)", cid));
        PushManager.getInstance().initialize(this.getApplicationContext(), DemoIntentService.class);
    }
}
