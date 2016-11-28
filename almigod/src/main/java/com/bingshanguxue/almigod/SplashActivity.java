package com.bingshanguxue.almigod;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.igexin.sdk.PushManager;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.InitActivity;

import butterknife.BindView;


/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {

    @BindView(R.id.tv_version)
    TextView tvVersion;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash_style01;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ZLogger.d("adb 0009");

        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        ZLogger.df("initializing getui sdk...");
        PushManager.getInstance().initialize(AlmigodApp.getAppContext());

        AppInfo appInfo = AnalysisAgent.getAppInfo(AlmigodApp.getAppContext());
        if (appInfo != null){
            tvVersion.setText(String.format("%s-%d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }

    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("ALMIGOD.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {
        //首次启动
        if(SharedPrefesManagerFactory.isAppFirstStart()){
            //清空旧缓存
//            AppHelper.clearAppCache();
            SharedPrefesManagerFactory.setTerminalId("");
            SharedPrefesManagerFactory.setAppFirstStart(false);
        }

//        AppHelper.clearOldPosOrder(7);
        ZLogger.deleteOldFiles(7);

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
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 500);
    }
}
