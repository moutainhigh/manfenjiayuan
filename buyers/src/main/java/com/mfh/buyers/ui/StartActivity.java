package com.mfh.buyers.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.igexin.sdk.PushManager;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.buyers.R;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * 欢迎页面
 * Created by 李潇阳 on 2014/11/13.
 */
public class StartActivity extends InitActivity {
    private BroadcastReceiver receiver = null;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        ZLogger.d("Initialize－－initializing getui sdk...");
        PushManager.getInstance().initialize(MfhApplication.getAppContext());
    }

    @Override
    protected void onDestroy() {
        if(receiver != null){
            unregisterReceiver(receiver);
        }

        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        DbVersion.setDomainVersion("BUYERS.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIHelper.redirectToActivity(StartActivity.this, MainTabActivity.class);
                finish();
            }
        }, 500);
    }

}
