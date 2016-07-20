package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.AppHelper;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
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

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        ZLogger.df("Initialize－－initializing getui sdk...");
        PushManager.getInstance().initialize(AppContext.getAppContext());
//        PushManager.getInstance().stopService(this);

        tvVersion.setText(String.format("%s-%d", AppContext.getVersionName(), AppContext.getVersionCode()));
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
            ZLogger.d(String.format("首次启动:%s-%s", AppContext.getVersionName(), AppContext.getVersionCode()));

            SharedPreferencesManager.setTerminalId("");
            SharedPreferencesManager.setAppFirstStart(false);
        }else{
            ZLogger.d(String.format("非首次启动:%s-%s", AppContext.getVersionName(),  AppContext.getVersionCode()));
        }


        AppHelper.clearOldPosOrder(15);

        onInitializedCompleted();
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
