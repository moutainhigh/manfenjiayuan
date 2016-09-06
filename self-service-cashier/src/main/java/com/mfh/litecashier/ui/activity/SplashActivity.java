package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.AnalysisAgent;
import com.mfh.framework.anlaysis.AppInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.PurchaseShopcartHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import butterknife.Bind;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 开屏页
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
 */
public class SplashActivity extends InitActivity {

    @Bind(R.id.tv_version)
    TextView tvVersion;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
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

        ZLogger.d("adb 000019");
        // SDK初始化，第三方程序启动时，都要进行SDK初始化工作,（注：每个应用程序只能初始化一次SDK，使用一个推送通道）
//        初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
        ZLogger.df("initializing getui sdk...");
        PushManager.getInstance().initialize(CashierApp.getAppContext());
//        PushManager.getInstance().stopService(this);

        AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());
        if (appInfo != null){
            tvVersion.setText(String.format("%s-%d",
                    appInfo.getVersionName(), appInfo.getVersionCode()));
        }
        else{
            tvVersion.setText("");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        ZLogger.df("set database version.");
        DbVersion.setDomainVersion("LITECASHIER.CLIENT.DB.UPGRADE", 16);
    }

    @Override
    protected void initSecondary() {
        super.initSecondary();
    }

    @Override
    protected void initComleted() {

        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                AppHelper.saveAppStartupDatetime();
                AppInfo appInfo = AnalysisAgent.getAppInfo(MfhApplication.getAppContext());

                //首次启动(由于应用程序可能会被多次执行在不同的进程中，所以这里在启动页调用)
                ZLogger.df(String.format("应用程序启动(%s-%d)：process: %s",
                        appInfo.getVersionName(), appInfo.getVersionCode(),
                        CashierApp.getProcessName(CashierApp.getAppContext(),
                                android.os.Process.myPid())));

                if (SharedPreferencesManager.isAppFirstStart()) {
                    SharedPreferencesHelper.setSyncProductsCursor("");
                    SharedPreferencesHelper.setPosOrderLastUpdate("");
                    SharedPreferencesManager.setTerminalId("");
                    SharedPreferencesManager.setSoftKeyboardEnabled(false);

                    SharedPreferencesManager.setAppFirstStart(false);
                } else {
                    CashierShopcartService.getInstance().clear();//购物车－收银
                    PurchaseShopcartHelper.getInstance().clear();//购物车－采购
                    PosCategoryGodosTempService.getInstance().clear();
                }

                AppHelper.clearRedunantData(false);

                //加载会话和群组
                IMClient.getInstance().groupManager().loadAllGroups();
                IMClient.getInstance().chatManager().loadAllConversations();

                //验证登录状态是否有效
                if (MfhLoginService.get().haveLogined()) {
                    subscriber.onNext(true);
                } else {
                    MfhLoginService.get().clear();
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            redirectToMain(true);
                        } else {
                            redirectToLogin();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_NATIVE_LOGIN: {
                if (resultCode == Activity.RESULT_OK) {
                    redirectToMain(false);
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 跳转至登录
     */
    private void redirectToLogin() {
        ZLogger.df("准备跳转到登录页");

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
    }

    /**
     * 初始化完成,跳转至首页
     */
    private void redirectToMain(boolean delayed) {
        ZLogger.df("准备跳转到首页");
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

}
