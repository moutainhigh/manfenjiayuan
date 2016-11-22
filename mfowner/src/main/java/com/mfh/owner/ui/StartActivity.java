package com.mfh.owner.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.igexin.sdk.PushManager;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.owner.AppHelper;
import com.mfh.owner.R;
import com.mfh.owner.wxapi.WXConstants;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 欢迎页面
 * Created by bingshanguxue on 2014/11/13.
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
        return R.layout.owner_start_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化个推服务
        PushManager.getInstance().initialize(this.getApplicationContext());

        //注册应用到微信
        registerToWx();

        //首次启动

        if(SharedPrefesManagerFactory.isAppFirstStart()){
            //清空旧缓存
            AppHelper.clearAppCache();

            SharedPrefesManagerFactory.setAppFirstStart(false);
        }
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
        DbVersion.setDomainVersion("OWNER.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                com.mfh.framework.uikit.UIHelper.startActivity(StartActivity.this, MainTabActivity.class);
                finish();
            }
        }, 500);
    }



    /**
     * 注册应用id到微信
     * */
    private void registerToWx(){
        IWXAPI wxApi = WXAPIFactory.createWXAPI(this, WXConstants.APP_ID, false);
        wxApi.registerApp(WXConstants.APP_ID);
    }
}
