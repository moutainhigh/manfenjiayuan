package com.manfenjiayuan.loveshopping.activity;

import android.os.Bundle;
import android.os.Handler;

import com.manfenjiayuan.loveshopping.R;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.UIHelper;

public class SplashActivity extends InitActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        ZLogger.df("Initialize--set database version.");
        DbVersion.setDomainVersion("LOVESHOPPING.CLIENT.DB.UPGRADE", 4);

//        AnalysisHelper.validateHandoverInfo();
    }

    @Override
    protected void initComleted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIHelper.startActivity(SplashActivity.this, NavigationActivity.class);
                finish();
            }
        }, 500);
    }
}
