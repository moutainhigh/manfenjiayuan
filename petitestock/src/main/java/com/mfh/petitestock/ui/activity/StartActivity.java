package com.mfh.petitestock.ui.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.InitActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.AppHelper;
import com.mfh.petitestock.R;

/**
 * 欢迎页面
 * Created by Nat.ZZN(bingshanguxue) on 2015/9/13.
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
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化个推SDK服务，该方法必须在Activity或Service类内调用，不建议在Application继承类中调用。
//        PushManager.getInstance().initialize(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initPrimary() {
        super.initPrimary();

        DbVersion.setDomainVersion("PETITESTOCK.CLIENT.DB.UPGRADE", 0);
    }

    @Override
    protected void initComleted() {
        AppHelper.clearOldPosOrder(15);
        //  注册设备
        registerTerminal();
    }

    /**
     * 注册设备
     * */
    protected void registerTerminal(){
        //TODO 请求设备终端编号
        if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId()) && NetWorkUtil.isConnect(AppContext.getAppContext())) {
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            if (rspData != null) {
                                RspValue<String> retValue = (RspValue<String>) rspData;
                                String retStr = retValue.getValue();
                                ZLogger.d("初始化－－获取设备号成功:" + retStr);
                                SharedPreferencesManager.setTerminalId(retStr);
                                onInitializedCompleted();
                            }
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.d(String.format("初始化－－获取设备号失败(%s),请在设置中手动激活", errMsg));
                            onInitializedCompleted();
                        }
                    }
                    , String.class
                    , AppContext.getAppContext()) {
            };

            JSONObject order = new JSONObject();
            order.put("serialNo", AppContext.getWifiMac15Bit());

            ZLogger.d("初始化－－注册设备," + order.toJSONString());
            MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
        } else {
            ZLogger.d("初始化－－设备号: " + SharedPreferencesManager.getTerminalId());
            onInitializedCompleted();
        }
    }

    /**
     *  初始化完成
     */
    private void onInitializedCompleted(){
        ZLogger.d("应用程序初始化完成。");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.actionStart(StartActivity.this, null);
                finish();
            }
        }, 500);
    }
}
