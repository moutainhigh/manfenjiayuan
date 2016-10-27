package com.mfh.litecashier.service;


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
import com.mfh.framework.api.analysis.AnalysisApiImpl;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.posRegister.PosRegisterApi;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.event.AffairEvent;

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
 * 1.{@link #batchValidate()} 批量验证<br>
 * 2.{@link #stepValidate(int)} 单步验证 <br>    </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ValidateManager {

    public static final int STEP_VALIDATE_NA = -1;
    public static final int STEP_VALIDATE_SESSION = 0;// 检查是否已经登录/会话是否有效
    /**
     * 设备注册,每次启动都需要注册，由后台去判断是否需要注册
     */
    public static final int STEP_REGISTER_PLAT = 1;//
    public static final int STEP_HAVENOMENYEND = 2;// 检查是否清分余额不足
    public static final int STEP_VALIDATE_CASHQUOTA = 3;//统计分析现金授权额度


    private boolean bSyncInProgress = false;//是否正在同步
    private int nextStep = STEP_VALIDATE_NA;

    private static ValidateManager instance = null;

    /**
     * 返回 DataSyncManagerImpl 实例
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
     * 批量验证
     */
    public synchronized void batchValidate() {
        if (bSyncInProgress) {
            ZLogger.df("正在验证数据...");
            return;
        }

        processStep(STEP_VALIDATE_SESSION, STEP_REGISTER_PLAT);
    }

    /**
     * 单步验证
     */
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
    private void nextStep() {
        processStep(nextStep, nextStep + 1);
    }

    private void processStep(int step, int nextStep) {
        this.nextStep = nextStep;
        switch (step) {
            case STEP_VALIDATE_SESSION: {
                checkSessionExpire();
            }
            break;
            case STEP_REGISTER_PLAT: {
//                if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())){
                registerPlat();
//                }
//                else{
//                    nextStep();
//                }
            }
            break;
            case STEP_HAVENOMENYEND: {
//                validateCheckHaveDateEndByServer();
                haveNoMoneyEnd();
            }
            break;
            case STEP_VALIDATE_CASHQUOTA: {
                needLockPos();
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
     */
    private void validateFinished(int eventId, Bundle args, String msg) {
        if (!StringUtils.isEmpty(msg)) {
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
     * 登录状态验证:进入需要登录的功能时需要
     */
    private void checkSessionExpire() {
        if (!MfhLoginService.get().haveLogined()) {
            validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN,
                    null, "未登录，跳转到登录页面");
            return;
        }

        //已经登录,检查会话是否过期
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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
                    , CashierApp.getAppContext()) {
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
                        validateFinished(ValidateManagerEvent.EVENT_ID_INTERRUPT_NEED_LOGIN,
                                null, "登录已失效－－" + errMsg);
                    }
                });
    }


    /**
     * 注册设备，报告版本并同步日期
     */
    private void registerPlat() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())) {
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

    private NetCallBack.NetTaskCallBack posRegisterCreateRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"新增成功!","version":"1","data":"67,2016-08-22 13:09:57"}
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

                    if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())) {
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
                        SharedPreferencesManager.setTerminalId(retA[0]);
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
                        if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())) {
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
     * 针对当前用户所属网点判断是否存在过清分时余额不足情况
     * /analysisAccDate/haveNoMoneyEnd?date=2016-02-02
     *
     * @param request date可空,默认是昨天。代表昨天包括昨天以前的时间内有无存在余额不足情况。
     */
    private void haveNoMoneyEnd() {
//        Calendar rightNow = Calendar.getInstance();
//        rightNow.add(Calendar.DAY_OF_MONTH, -1);
//        final Date yesterday = rightNow.getTime();
//
//        final String aggDateStr = TimeCursor.FORMAT_YYYYMMDD.format(yesterday);
//        ZLogger.df(String.format("检测 %s 是否清分完毕", aggDateStr));

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            AlarmManagerHelper.triggleNextDailysettle(0);

            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证(昨日是否已经清分)。");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"10.4"}}
                        try {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            Double amount = Double.valueOf(retValue.getValue());
                            if (amount >= 0.01) {
                                Bundle args = new Bundle();
                                args.putDouble("amount", amount);
                                validateFinished(ValidateManagerEvent.EVENT_ID_INCOME_DESTRIBUTION_TOPUP,
                                        args, String.format("余额不足(%2f)清分失败，即将锁定POS机，" +
                                                "可以通过提交营业现金来解锁", amount));

                                AlarmManagerHelper.triggleNextDailysettle(1);
                            } else {
                                ZLogger.df(String.format("清分完成: %.2f, 可以正常使用POS机", amount));

                                nextStep();
                            }

                        } catch (NumberFormatException e) {
                            e.printStackTrace();

                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("判读是否清分失败：" + errMsg);
                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        AnalysisApiImpl.haveNoMoneyEnd(responseCallback);
    }

    /**
     * 判断是否需要锁定pos
     */
    private void needLockPos() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            AlarmManagerHelper.triggleNextDailysettle(0);

            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证(昨日是否已经清分)。");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":"false,8.99"}
                        try {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String result = retValue.getValue();
                            String[] ret = result.split(",");
                            if (ret.length >= 2) {
//                                Boolean.parseBoolean()1
//                                boolean isNeedLock = Boolean.valueOf(ret[0]).booleanValue();
                                boolean isNeedLock = Boolean.parseBoolean(ret[0]);
                                Double amount = Double.valueOf(ret[1]);

                                ZLogger.df(String.format("判断是否需要锁定POS机，isNeedLock=%b, amount=%.2f",
                                        isNeedLock, amount));
                                if (isNeedLock && amount >= 0.01) {
                                    Bundle args = new Bundle();
                                    args.putDouble("amount", amount);
                                    validateFinished(ValidateManagerEvent.EVENT_ID_CASH_QUOTA_TOPUP,
                                            args, String.format("现金超过授权金额(%2f)，即将锁定POS机，" +
                                                    "可以通过提交营业现金来解锁", amount));
                                    AlarmManagerHelper.triggleNextDailysettle(1);
                                } else {
                                    AlarmManagerHelper.triggleNextDailysettle(0);
                                    EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_UNLOCK_POS_CLIENT));
                                    nextStep();
                                }
                            } else {
                                ZLogger.df("判断是否需要锁定POS机:" + result);
                                AlarmManagerHelper.triggleNextDailysettle(0);
                                nextStep();
                            }
                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
                            ZLogger.ef(e.toString());

                            AlarmManagerHelper.triggleNextDailysettle(0);
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("判读是否锁定POS机失败：" + errMsg);
                        nextStep();

                        AlarmManagerHelper.triggleNextDailysettle(0);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.needLockPos(responseCallback);
    }


    public class ValidateManagerEvent {
        public static final int EVENT_ID_INTERRUPT_NEED_LOGIN = 0X02;//需要登录
        public static final int EVENT_ID_INTERRUPT_PLAT_NOT_REGISTER = 0X04;//设备未注册
        public static final int EVENT_ID_RETRY_SIGNIN_SUCCEED = 0X03;//重登录成功
        public static final int EVENT_ID_INCOME_DESTRIBUTION_TOPUP = 0x06;//清分充值
        public static final int EVENT_ID_CASH_QUOTA_TOPUP = 0x07;//授权额度超限充值
        public static final int EVENT_ID_VALIDATE_FINISHED = 0X08;//验证结束
        public static final int EVENT_ID_VALIDATE_QUOTA_UPDATE = 0X09;//额度发生变化

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
