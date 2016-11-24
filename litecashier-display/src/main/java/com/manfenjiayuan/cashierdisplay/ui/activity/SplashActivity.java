package com.manfenjiayuan.cashierdisplay.ui.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.igexin.sdk.PushManager;
import com.manfenjiayuan.cashierdisplay.AppContext;
import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;


/**
 * 开屏页
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {


    public static final int ARC_NATIVE_LOGIN                    = 0x02;//登录

    private boolean initializeCompleted;

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
    protected boolean isBackKeyEnabled() {
        return false;
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        ZLogger.df("set database version.");
        DbVersion.setDomainVersion("CASHIERDISPLAY.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initSecondary() {
        super.initSecondary();
    }

    @Override
    protected void initComleted() {

        /**
         *
         首次启动(由于应用程序{@link com.mfh.litecashier.CashierApp}可能会被多次执行在不同的进程中，所以这里在启动页调用，)
         */
        if(SharedPrefesManagerFactory.isAppFirstStart()){
            ZLogger.df(String.format("application first running: %s-%s(%s)",
                    AppContext.getVersionName(), AppContext.getVersionCode(),
                    AppContext.getProcessName(AppContext.getAppContext(), android.os.Process.myPid())));

            SharedPrefesManagerFactory.setTerminalId("");
            SharedPrefesManagerFactory.setSoftInputEnabled(false);
//            SharedPreferencesHelper.setPosOrderSyncInterval(15 * 60);//15分钟同步一次
//            SharedPreferencesHelper.setSyncIntervalCompanyHuman(30 * 60);//30分钟同步一次

            SharedPrefesManagerFactory.setAppFirstStart(false);
        }else{
            ZLogger.df(String.format("application running: %s-%s(%s)",
                    AppContext.getVersionName(), AppContext.getVersionCode(),
                    AppContext.getProcessName(AppContext.getAppContext(), android.os.Process.myPid())));

            //清空旧缓存
//            AppHelper.clearAppCache();
            //清除数据缓存
//            DataCleanManager.cleanInternalCache(getApplicationContext());
        }

        // 清空缓存数据

        //加载会话和群组
        IMClient.getInstance().groupManager().loadAllGroups();
        IMClient.getInstance().chatManager().loadAllConversations();

        onInitializedCompleted();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirectToMain(false);
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *  初始化完成
     */
    private void onInitializedCompleted(){
        //验证登录状态是否有效
        if (MfhLoginService.get().haveLogined()) {
            if (NetworkUtils.isConnect(AppContext.getAppContext())) {
                ZLogger.df("login already，validate session");
                validSession();
            } else {
                ZLogger.df("login already，redirect to main page");
                redirectToMain(true);
            }
        } else {
            ZLogger.df("not login，retry login");
            redirectToLogin();
        }
    }

    /**
     * 跳转至登录
     */
    private void redirectToLogin() {
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);

        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARC_NATIVE_LOGIN);
    }

    /**
     * 跳转至首页
     */
    private void redirectToMain(boolean delayed) {
        if (delayed) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.actionStart(SplashActivity.this, null);
                    finish();
                }
            }, 500);
        } else {
            MainActivity.actionStart(SplashActivity.this, null);
            finish();
        }

    }

    /**
     * 登录状态验证:进入需要登录的功能时需要
     */
    private void validSession() {
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        ZLogger.df("登录状态有效 ");
                        redirectToMain(true);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("登录状态已失效, " + errMsg);

                        //已过期，跳转到登录页面
                        //已过期，自动重登录
//                        animProgress.setVisibility(View.GONE);
                        redirectToLogin();
                    }
                }
                , String.class
                , AppContext.getAppContext()) {
        };

        UserApiImpl.validSession(responseCallback);
    }
}
