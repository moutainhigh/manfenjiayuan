package com.mfh.enjoycity.ui.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.igexin.sdk.PushManager;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.enjoycity.AppContext;
import com.mfh.enjoycity.R;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.UIHelper;

/**
 * 欢迎页面
 * Created by 李潇阳 on 2014/11/13.
 */
public class StartActivity extends InitActivity {

    public static PendingIntent generatePendingIntent(Context context){
        Intent intent =new Intent(context, StartActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FILL_IN_DATA);
        intent.setAction(String.valueOf(System.currentTimeMillis()));

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        ZLogger.d("Initialize－－initializing getui sdk...");
        PushManager.getInstance().initialize(MfhApplication.getAppContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        ZLogger.d("Initialize--set database version.");
        DbVersion.setDomainVersion("ENJOYCITY.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {

        /**
         *
         首次启动(由于应用程序{@link com.mfh.litecashier.CashierApp}可能会被多次执行在不同的进程中，所以这里在启动页调用，)
         */
        if(SharedPreferencesManager.isAppFirstStart()){
            ZLogger.d(String.format("Initialize--application first running: %s-%s(%s)",
                    AppContext.getVersionName(), AppContext.getVersionCode(),
                    AppContext.getProcessName(AppContext.getAppContext(), android.os.Process.myPid())));
            SharedPreferencesManager.setTerminalId("");
            SharedPreferencesManager.setSoftKeyboardEnabled(false);
//            SharedPreferencesHelper.setPosOrderSyncInterval(15 * 60);//15分钟同步一次
//            SharedPreferencesHelper.setSyncIntervalCompanyHuman(30 * 60);//30分钟同步一次

            SharedPreferencesManager.setAppFirstStart(false);
        }else{
            ZLogger.d(String.format("Initialize--application running: %s-%s(%s)",
                    AppContext.getVersionName(), AppContext.getVersionCode(),
                    AppContext.getProcessName(AppContext.getAppContext(), android.os.Process.myPid())));

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIHelper.startActivity(StartActivity.this, MainActivity.class);
                finish();
            }
        }, 500);
    }

}
