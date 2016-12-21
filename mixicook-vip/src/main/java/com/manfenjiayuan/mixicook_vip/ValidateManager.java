package com.manfenjiayuan.mixicook_vip;


import android.os.Bundle;
import android.os.SystemClock;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.api.posRegister.PosRegisterApi;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.Date;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * <h1>POS--验证</h1>
 * <p>进入首页后开始启动验证<br>
 *     1.{@link #batchValidate()} 批量验证<br>
 *     2.{@link #stepValidate(int)} 单步验证 <br>    </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ValidateManager {

    public static final int STEP_VALIDATE_NA = -1;
    public static final int STEP_VALIDATE_SESSION = 0;// 检查是否已经登录/会话是否有效
    /**设备注册,每次启动都需要注册，由后台去判断是否需要注册*/
    public static final int STEP_REGISTER_PLAT = 1;//

    private boolean bSyncInProgress = false;//是否正在同步
    private int nextStep = STEP_VALIDATE_NA;

    private static ValidateManager instance = null;

    /**
     * 返回 DataSyncManager 实例
     *
     * @return
     */
    public static ValidateManager get() {
        if (instance == null) {
            synchronized (ValidateManager.class) {
                if (instance == null) {
                    instance = new ValidateManager();
                }
            }
        }
        return instance;
    }

    /**
     *  批量验证
     */
    public synchronized void batchValidate() {
        if (bSyncInProgress) {
            ZLogger.df("正在验证数据...");
            return;
        }

        processStep(STEP_VALIDATE_SESSION, STEP_REGISTER_PLAT);
    }

    /**
     * 单步验证*/
    public void stepValidate(int step) {
        if (bSyncInProgress) {
            ZLogger.df("正在验证数据...");
            return;
        }

        processStep(step, STEP_VALIDATE_NA);
    }

    /**
     * 下一步
     */
    private void nextStep(){
        processStep(nextStep, nextStep+1);
    }

    private void processStep(int step, int nextStep) {
        this.nextStep = nextStep;
        switch (step) {
            case STEP_VALIDATE_SESSION: {
                checkSessionExpire();
            }
            break;
            case STEP_REGISTER_PLAT: {
//                if (StringUtils.isEmpty(SharedPrefesManagerFactory.getTerminalId())){
                    registerPlat();
//                }
//                else{
//                    nextStep();
//                }
            }
            break;
            default: {
                validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null, "验证结束");
            }
            break;
        }
    }

    /**
     * 验证结束
     * */
    private void validateFinished(int eventId, Bundle args, String msg) {
        if (!StringUtils.isEmpty(msg)){
            ZLogger.df(msg);
        }
        bSyncInProgress = false;
        EventBus.getDefault().post(new ValidateManagerEvent(eventId, args));
    }

    /**
     * 验证结束
     */
    private void validateUpdate(int eventId, Bundle args, String msg) {
        if (!StringUtils.isEmpty(msg)) {
            ZLogger.df(msg);
        }
        EventBus.getDefault().post(new ValidateManagerEvent(eventId, args));
    }

    /**
     * 注册设备
     * */
    private void registerPlat(){
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            if (StringUtils.isEmpty(SharedPrefesManagerFactory.getTerminalId())) {
                validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER,
                        null, "设备注册失败，需要重新注册");
            } else {
                validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                        "网络未连接，暂停注册设备。");
            }
        } else {
            JSONObject order = new JSONObject();
            order.put("serialNo", SystemUtils.getDeviceUuid(MfhApplication.getAppContext()));
//        order.put("serialNo", MfhApplication.getWifiMac15Bit());
            order.put("channelId", MfhApi.CHANNEL_ID);
            order.put("channelPointId", IMConfig.getPushClientId());
            order.put("netId", MfhLoginService.get().getCurOfficeId());
            ZLogger.df("注册设备中..." + order.toJSONString());
            PosRegisterApi.create(order.toJSONString(), posRegisterCreateRC);
        }
    }

    NetCallBack.NetTaskCallBack posRegisterCreateRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    if (rspData != null) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String retStr = retValue.getValue();
                        ZLogger.df("注册设备成功:" + retStr);
                        saveTerminalId(retStr);
                    }
                    else{
                        saveTerminalId(null);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df(String.format("注册设备失败,%s", errMsg));

                    if (StringUtils.isEmpty(SharedPrefesManagerFactory.getTerminalId())) {
                        validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER,
                                null, "设备注册失败，需要重新注册");
                    } else {
                        nextStep();
                    }
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 保存设备编号
     * */
    private void saveTerminalId(final String respnse){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (!StringUtils.isEmpty(respnse)){
                    ZLogger.df("注册设备成功:" + respnse);
                    String[] retA = respnse.split(",");
                    if (retA.length > 1){
                        SharedPrefesManagerFactory.setTerminalId(retA[0]);
                        // TODO: 8/22/16 修改本地系统时间
                        ZLogger.d(String.format("当前系统时间1: %s",
                                TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                        Date serverDateTime = TimeUtil.parse(retA[1], TimeUtil.FORMAT_YYYYMMDDHHMMSS);
//                                Date serverDateTime = TimeUtil.parse("2016-08-22 13:09:57", TimeUtil.FORMAT_YYYYMMDDHHMMSS);
                        if (serverDateTime != null){
                            //设置时间
                            try{
                                boolean isSuccess = SystemClock.setCurrentTimeMillis(serverDateTime.getTime());
                                ZLogger.d(String.format("修改系统时间 %b: %s", isSuccess,
                                        TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
                            }
                            catch (Exception e){
                                ZLogger.ef("修改系统时间失败:" + e.toString());
                            }
                        }
                    }
                }

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
                        nextStep();
                    }

                    @Override
                    public void onNext(String startCursor) {
                        if (StringUtils.isEmpty(SharedPrefesManagerFactory.getTerminalId())) {
                            validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER,
                                    null, "设备注册失败，需要重新注册");
                        } else {
                            if (!BizConfig.RELEASE){
                                validateUpdate(ValidateManagerEvent.EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER,
                                        null, "测试注册设备功能");
                            }
                            nextStep();
                        }

                    }

                });
    }

    /**
     * 登录状态验证:进入需要登录的功能时需要
     */
    private void checkSessionExpire() {
        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.d("检测账号未登录，准备跳转到登录页面");

            validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN,
                    null, "未登录，跳转到登录页面");
            return;
        }

        //已经登录,检查会话是否过期
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            nextStep();
        } else {
            NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"登录成功!","version":"1","data":""}
                            ZLogger.df("验证登录状态成功");
                            nextStep();
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            retryLogin();
                        }
                    }
                    , String.class
                    , AppContext.getAppContext()) {
            };

            UserApiImpl.validSession(responseCallback);
        }
    }

    /**
     * 自动重登录
     */
    private void retryLogin() {
        MfhLoginService.get().doLoginAsync(MfhLoginService.get().getLoginName(),
                MfhLoginService.get().getPassword(), new LoginCallback() {
                    @Override
                    public void loginSuccess(UserMixInfo user) {
                        //登录成功
                        ZLogger.df("重登录成功：");

                        //注册到消息桥
                        IMClient.getInstance().registerBridge();

                        validateUpdate(ValidateManagerEvent.EVENT_ID_RETRY_SIGNIN_SUCCEED,
                                null, "重登录成功");
                        nextStep();
                    }

                    @Override
                    public void loginFailed(String errMsg) {
                        ZLogger.d("账号重登录失败，账号失效，准备跳转到登录页面");

                        validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN,
                                null, "登录已失效－－" + errMsg);
                    }
                });
    }

    public class ValidateManagerEvent {
        public static final int EVENT_ID_VALIDATE_START             = 0X01;//验证开始
        public static final int EVENT_ID_INTERRUPT_NEED_LOGIN        = 0X02;//需要登录
        public static final int EVENT_ID_RETRY_SIGNIN_SUCCEED = 0X03;//重登录成功
        public static final int EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER = 0X04;//设备未注册
        public static final int EVENT_ID_VALIDATE_FINISHED          = 0X06;//验证结束

        private int eventId;
        private Bundle args;//参数

        public ValidateManagerEvent(int eventId) {
            this.eventId = eventId;
        }

        public ValidateManagerEvent(int eventId, Bundle args) {
            this.eventId = eventId;
            this.args = args;
        }

        public int getEventId() {
            return eventId;
        }

        public Bundle getArgs() {
            return args;
        }
    }

}
