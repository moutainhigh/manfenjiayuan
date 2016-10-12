package com.manfenjiayuan.mixicook_vip.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.manfenjiayuan.business.ui.SignInActivity;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.utils.AppHelper;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;

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
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("PDASUPERMARKET.CLIENT.DB.UPGRADE", 0);
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
            case ARCode.ARC_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirect2Main();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
