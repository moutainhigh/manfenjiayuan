package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.cashier.database.service.PosTopupService;
import com.bingshanguxue.vector_user.UserApiImpl;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.hardware.SMScale.SMScaleSyncManager2;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.AppHelper;
import com.mfh.litecashier.utils.CashierHelper;
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

        tvVersion.setText(String.format("%s-%d",
                CashierApp.getVersionName(), CashierApp.getVersionCode()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();
        ZLogger.df("set database version.");
        DbVersion.setDomainVersion("LITECASHIER.CLIENT.DB.UPGRADE", 15);
    }

    @Override
    protected void initSecondary() {
        super.initSecondary();
    }

    @Override
    protected void initComleted() {

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

                AppHelper.saveAppStartupDatetime();
                /**
                 *
                 首次启动(由于应用程序{@link com.mfh.litecashier.CashierApp}可能会被多次执行在不同的进程中，所以这里在启动页调用，)
                 */
                ZLogger.df(String.format("application running: %s_%s-%s",
                        CashierApp.getProcessName(CashierApp.getAppContext(), android.os.Process.myPid()),
                        CashierApp.getVersionName(), CashierApp.getVersionCode()));

                if (SharedPreferencesManager.isAppFirstStart()) {
                    ZLogger.df(String.format("application first running: %s-%s(%s)",
                            CashierApp.getVersionName(), CashierApp.getVersionCode(),
                            CashierApp.getProcessName(CashierApp.getAppContext(), android.os.Process.myPid())));
                    SharedPreferencesHelper.setSyncProductsCursor("");
                    SharedPreferencesHelper.setPosOrderLastUpdate("");
                    SharedPreferencesManager.setTerminalId("");
                    SharedPreferencesManager.setSoftKeyboardEnabled(false);
//            SharedPreferencesHelper.setPosOrderSyncInterval(15 * 60);//15分钟同步一次
//            SharedPreferencesHelper.setSyncIntervalCompanyHuman(30 * 60);//30分钟同步一次

                    SharedPreferencesManager.setAppFirstStart(false);
                } else {
                    //清空旧缓存
//            AppHelper.clearAppCache();
                    //清除数据缓存
//            DataCleanManager.cleanInternalCache(getApplicationContext());
//            SharedPreferencesHelper.setPosOrderSyncInterval(25 * 60);
//            SharedPreferencesHelper.setSyncCompanyHumanInterval(30 * 60);

                    CashierShopcartService.getInstance().clear();//购物车－收银
                    PurchaseShopcartHelper.getInstance().clear();//购物车－采购
                    AnalysisHelper.deleteOldDailysettle(7);//日结
                    CashierHelper.clearOldPosOrder(15);//收银订单
                    PosTopupService.get().deleteOldData(15);
                    SMScaleSyncManager2.deleteOldFiles(1);
                    SMScaleSyncManager2.deleteOldFiles2();

                    ZLogger.deleteOldFiles(7);
                }

                // 清空缓存数据
                AppHelper.clearTempData();

                //加载会话和群组
                IMClient.getInstance().groupManager().loadAllGroups();
                IMClient.getInstance().chatManager().loadAllConversations();

                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String startCursor) {

                        onInitializedCompleted();
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
     * 初始化完成
     */
    private void onInitializedCompleted() {
        //验证登录状态是否有效
        if (MfhLoginService.get().haveLogined()) {
            if (NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                validSession();
            } else {
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

        Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_NATIVE_LOGIN);
    }

    /**
     * 跳转至首页
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
                , CashierApp.getAppContext()) {
        };

        ZLogger.df(">>检查会话是否有效");
        UserApiImpl.validSession(responseCallback);
    }
}
