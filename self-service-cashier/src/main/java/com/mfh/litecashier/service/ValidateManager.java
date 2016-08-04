package com.mfh.litecashier.service;


import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.DailysettleService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.bingshanguxue.cashier.model.wrapper.PayWay;
import com.bingshanguxue.vector_user.UserApiImpl;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.analysis.AnalysisApiImpl;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.core.DeviceUuidFactory;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.QuotaEntity;
import com.mfh.litecashier.database.logic.QuotaService;
import com.mfh.litecashier.utils.AlarmManagerHelper;
import com.mfh.litecashier.utils.AnalysisHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * <h1>POS--验证</h1>
 * <p>进入首页后开始启动验证<br>
 *     1.{@link #batchValidate()} 批量验证<br>
 *     2.{@link #stepValidate(int)} 单步验证 <br>    </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ValidateManager {

    private static final String TAG = "ValidateManager";

    public static final int STEP_VALIDATE_NA = -1;
    public static final int STEP_VALIDATE_SESSION = 0;// 检查是否已经登录/会话是否有效
    /**设备注册,每次启动都需要注册，由后台去判断是否需要注册*/
    public static final int STEP_REGISTER_PLAT = 1;//
    public static final int STEP_VALIDATE_ANALYSISACCDATE_HAVEDATEEND = 2;// 检查是否清分完毕
    public static final int STEP_VALIDATE_ANALISIS_QUOTA = 4;//统计分析现金授权额度


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
//                if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())){
                    registerPlat();
//                }
//                else{
//                    nextStep();
//                }
            }
            break;
            case STEP_VALIDATE_ANALYSISACCDATE_HAVEDATEEND: {
                validateCheckHaveDateEndByServer();
            }
            break;
            case STEP_VALIDATE_ANALISIS_QUOTA: {
                analysisQuota(new Date());
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
     * 登录状态验证:进入需要登录的功能时需要
     * */
    private void checkSessionExpire(){
        //未登录
        if (!MfhLoginService.get().haveLogined()) {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN,
                    null, "未登录，跳转到登录页面");
        }
        //已经登录,检查会话是否过期
        else {
            if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                nextStep();
//                //没有网络，跳过验证会话过期，直接进行下一步操作
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN,
// null, "未登录，跳转到登录页面");
//                    }
//                }, 1000);
            } else {
                NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                        NetProcessor.Processor<String>>(
                        new NetProcessor.Processor<String>() {
                            @Override
                            public void processResult(IResponseData rspData) {
                                //{"code":"0","msg":"登录成功!","version":"1","data":""}
                                ZLogger.df("登录状态有效 ");
                                nextStep();
                            }

                            @Override
                            protected void processFailure(Throwable t, String errMsg) {
                                super.processFailure(t, errMsg);
                                validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED,
                                        null, "会话过期，自动重登录");
                            }
                        }
                        , String.class
                        , CashierApp.getAppContext()) {
                };

                UserApiImpl.validSession(responseCallback);
            }
        }
    }


    /**
     * 注册设备
     * */
    private void registerPlat(){
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("注册设备成功:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("注册设备失败,%s", errMsg));

                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_PLAT_NOT_REGISTER,
                                null, "设备注册失败，需要重新注册");
                        nextStep();
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("serialNo", String.format("%s@%s",
                MfhApplication.getPackageInfo().packageName,
                new DeviceUuidFactory(MfhApplication.getAppContext()).getDeviceUuid()));
//        order.put("serialNo", MfhApplication.getWifiMac15Bit());
        order.put("channelId", MfhApi.CHANNEL_ID);
        order.put("channelPointId", IMConfig.getPushClientId());
        order.put("netId", MfhLoginService.get().getCurOfficeId());

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停注册设备。");
        }
        else {
            ZLogger.df("注册设备中..." + order.toJSONString());
            MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
        }
    }

    /**
     * 检测昨日是否清分完毕：针对当前用户所属网点判断是否进行过日结清分操作；如果发现未清分，则锁定pos机，
     * 但允许调用commintCashAndTrigDateEnd接口提交营业现金，后台马上会触发一次清分。
     * */
    private void validateCheckHaveDateEndByServer(){
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.DAY_OF_MONTH, -1);
        final Date yesterday = rightNow.getTime();

        final String aggDateStr = TimeCursor.FORMAT_YYYYMMDD.format(yesterday);
        ZLogger.df(String.format("检测 %s 是否清分完毕", aggDateStr));

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            //十分钟后自动重试
            Calendar trigger = Calendar.getInstance();
            trigger.add(Calendar.MINUTE, 10);
            AlarmManagerHelper.registerDailysettle(CashierApp.getAppContext(), trigger);

            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证(昨日是否已经清分)。");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Boolean,
                NetProcessor.Processor<Boolean>>(
                new NetProcessor.Processor<Boolean>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":false}
                        //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                        //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                        RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                        Boolean isHaveDateEnd = retValue.getValue();

                        final DailysettleEntity dailysettleEntity = AnalysisHelper.createDailysettle(yesterday);
                        if (dailysettleEntity == null){
                            ZLogger.df(String.format("创建日结单失败：%s", aggDateStr));

                            //十分钟后自动重试
                            Calendar trigger = Calendar.getInstance();
                            trigger.add(Calendar.MINUTE, 10);
                            AlarmManagerHelper.registerDailysettle(CashierApp.getAppContext(), trigger);

                            nextStep();
                            return;
                        }

                        if (isHaveDateEnd){
                            ZLogger.df(String.format("%s 已清分", aggDateStr));
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_YES);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);
                        }
                        else{
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_NO);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);

                            Bundle args = new Bundle();
                            args.putString("dailysettleDatetime",
                                    TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(yesterday));
                            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_DAILYSETTLE,
                                    args, String.format("%s 未清分，即将锁定POS机，" +
                                            "可以通过提交营业现金来解锁", aggDateStr));
                        }
                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("判读是否清分失败：" + errMsg);
                        nextStep();
                    }
                }
                , Boolean.class
                , CashierApp.getAppContext()) {
        };

        AnalysisApiImpl.analysisAccDateHaveDateEnd(yesterday, responseCallback);
    }


    /**
     * 统计分析,同时只能存在一条未支付的额度表，若存在多条，则默认取第一条。
     * */
    public void analysisQuota(Date analysisDate){
        try{
            QuotaEntity quotaEntity;

            String sqlWhere = String.format("payStatus != '%d'",
                    QuotaEntity.PAY_STATYS_PAID);
            List<QuotaEntity> entities = QuotaService.get().queryAllBy(sqlWhere, "updatedDate desc");
            if (entities != null && entities.size() > 0){
                quotaEntity = entities.get(0);
            }
            else{
                quotaEntity = new QuotaEntity();
                quotaEntity.setCreatedDate(new Date());
            }

            Double cashAmount = 0D;
            String orderSql = String.format("updatedDate >= '%s' and updatedDate < '%s' and sellerId = '%d' " +
                            "and status = '%d' and isActive = '%d'",
                    TimeUtil.format(quotaEntity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                    TimeUtil.format(analysisDate, TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                    MfhLoginService.get().getSpid(),
                    PosOrderEntity.ORDER_STATUS_FINISH, PosOrderEntity.ACTIVE);
            List<PosOrderEntity> orderEntities = PosOrderService.get().queryAllBy(orderSql);
            if (orderEntities != null && orderEntities.size() > 0){
                for (PosOrderEntity orderEntity : orderEntities){
                    OrderPayInfo orderPayInfo = OrderPayInfo.deSerialize(orderEntity.getId());
                    if (orderPayInfo == null){
                        continue;
                    }

                    List<PayWay> payWays = orderPayInfo.getPayWays();
                    if (payWays != null && payWays.size() > 0) {
                        for (PayWay payWay : payWays) {
                            if (WayType.CASH.equals(payWay.getPayType())) {
                                cashAmount += payWay.getAmount();
                            }
                        }
                    }
                }
            }

            quotaEntity.setAmount(cashAmount);
            quotaEntity.setUpdatedDate(analysisDate);

            QuotaService.get().saveOrUpdate(quotaEntity);
            ZLogger.d("现金金额授权更新:\n" + JSON.toJSONString(quotaEntity));

            Bundle args = new Bundle();
            args.putLong("orderId", quotaEntity.getId());
            args.putDouble("quotaAmount", cashAmount);
            if (cashAmount >= QuotaEntity.MAX_QUOTA){
                args.putBoolean("isNeedPay", false);
            }
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_QUOTA_UPDATE,
                    args, String.format("现金额度金额更新: %.2f", cashAmount));
        }
        catch(Exception e){
            ZLogger.e(String.format("统计分析现金额度失败, %s", e.toString()));
        }

        nextStep();
    }


    public class ValidateManagerEvent {
        public static final int EVENT_ID_VALIDATE_START             = 0X01;//验证开始
        public static final int EVENT_ID_VALIDATE_NEED_LOGIN        = 0X02;//需要登录
        public static final int EVENT_ID_VALIDATE_SESSION_EXPIRED   = 0X03;//会话过期
        public static final int EVENT_ID_VALIDATE_PLAT_NOT_REGISTER = 0X04;//设备未注册
        public static final int EVENT_ID_VALIDATE_NEED_DAILYSETTLE  = 0X05;//需要日结
        public static final int EVENT_ID_VALIDATE_FINISHED          = 0X06;//验证结束
        public static final int EVENT_ID_VALIDATE_QUOTA_UPDATE     = 0X07;//额度发生变化

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
