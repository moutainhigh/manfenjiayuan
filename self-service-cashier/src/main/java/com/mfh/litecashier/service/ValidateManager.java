package com.mfh.litecashier.service;


import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.vector_user.UserApiImpl;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.core.DeviceUuidFactory;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.database.entity.DailysettleEntity;
import com.mfh.litecashier.database.logic.DailysettleService;
import com.mfh.litecashier.utils.AlarmManagerHelper;
import com.mfh.litecashier.utils.AnalysisHelper;

import java.text.ParseException;
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
    public static final int STEP_VALIDATE_ANALYSISACCDATE_HAVEDATEEND = 2;// 检查是否日结
    public static final int STEP_VALIDATE_ANALYSISACCDATE_NEEDDATEEND = 3;// 检查是否需要日结

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
                validateSession();
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
                AlarmManagerHelper.registerDailysettle(CashierApp.getAppContext());
            }
            break;
            case STEP_VALIDATE_ANALYSISACCDATE_NEEDDATEEND: {
//                validateGetLastAggDate();
                validateCheckNeedDateEndByServer();
                AlarmManagerHelper.registerDailysettle(CashierApp.getAppContext());
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
     * 判断是否登录
     * */
    private void validateSession(){
        //已经登录
        if (MfhLoginService.get().haveLogined()) {
            //检查会话是否过期
            if (NetWorkUtil.isConnect(CashierApp.getAppContext())) {
                checkSessionExpire();
            } else {
                nextStep();
//                //没有网络，跳过验证会话过期，直接进行下一步操作
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN, null, "未登录，跳转到登录页面");
//                    }
//                }, 1000);
            }
        }
        //未登录
        else {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN, null, "未登录，跳转到登录页面");
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
     * 登录状态验证:进入需要登录的功能时需要
     */
    private void checkSessionExpire() {
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
                        ZLogger.df("" + errMsg);
                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED,
                                null, "会话过期，自动重登录");
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        UserApiImpl.validSession(responseCallback);
    }

    /**
     * 获取后台最后日结日期
     * */
    private void validateGetLastAggDate(){
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证是否日结。");
            return;
        }

        bSyncInProgress = true;

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":"2016-02-18"}
                        try{
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String lastAggDate = retValue.getValue();
                            ZLogger.df(String.format("获取网店在后台最后日结日期成功：%s", lastAggDate));
                            //根据最后一次日结日期来判断是否日结
                            validateCheckNeedDateEndByPos(lastAggDate);
                        }
                        catch (Exception e){
                            ZLogger.e(e.toString());
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("获取最后日结日期失败：" + errMsg);
                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };


        CashierApiImpl.analysisAccDateGetLastAggDate(responseCallback);
    }

    /**
     * POS判读是否日结
     * @param lastAggDate 后台上一次日结日期
     * */
    private void validateCheckNeedDateEndByPos(String lastAggDate){
        bSyncInProgress = true;

        //获取POS设备最新的订单更新日期
        final Date latestNeedAggDate = getLatestNeedAggDate();
        if (latestNeedAggDate == null){
            ZLogger.df("POS设备最新的需要日结日期为空，不需要日结。");
            nextStep();
            return;
        }

        String latestNeedAggDateStr = TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(latestNeedAggDate);
        ZLogger.df(String.format("获取POS机是否需要进行日结: %s(%s)", latestNeedAggDateStr, lastAggDate));

        //POS设备最新的需要日结日期与后台日结日期比较，判断是否需要日结。
        if (!StringUtils.isEmpty(lastAggDate)){
            try {
                Date LastAggDatetime = TimeCursor.FORMAT_YYYYMMDD.parse(lastAggDate);
                if (LastAggDatetime.after(latestNeedAggDate)){
                    ZLogger.df("POS设备最新的需要日结日期比后台日结日期晚，不需要日结。");
                    nextStep();
                    return;
                }
            } catch (ParseException e) {
//                e.printStackTrace();
                ZLogger.df(String.format("POS设备最新的需要日结日期与后台日结日期比较失败, %s", e.toString()));
                nextStep();
                return;
            }
        }

        Bundle args = new Bundle();
        args.putString("dailysettleDatetime", latestNeedAggDateStr);
        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_DAILYSETTLE, args,
                String.format("%s 未做日结，需要重新日结后才可以使用POS", latestNeedAggDateStr));
    }

    /**
     * 后台判读当天是否已经日结，并修改日结确认状态
     * */
    private void validateCheckHaveDateEndByServer(){
        Date currentDate = new Date();

        final String aggDateStr = TimeCursor.FORMAT_YYYYMMDD.format(currentDate);
        ZLogger.df(String.format("判断当天是否日结：%s ", aggDateStr));

        final DailysettleEntity dailysettleEntity = AnalysisHelper.createDailysettle(currentDate);
        if (dailysettleEntity == null){
            ZLogger.df(String.format("创建日结单失败：%s", aggDateStr));
            nextStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证(当天是否已经日结)。");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":false}
                        //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                        //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        if (!StringUtils.isEmpty(retValue.getValue()) && retValue.getValue().equals("true")){
                            ZLogger.df(String.format("日结单已经确认：%s", aggDateStr));
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_YES);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);
                        }
                        else{
                            ZLogger.df(String.format("日结单未确认：%s", aggDateStr));
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_NO);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);
                        }
                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("判读是否日结失败：" + errMsg);
                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.analysisAccDateHaveDateEnd(currentDate, responseCallback);
    }

    /**
     * 后台判读是否有日结未确认，并修改日结确认状态
     * */
    private void validateCheckNeedDateEndByServer(){
        //获取POS设备最新的订单更新日期
        final Date latestNeedAggDate = getLatestNeedAggDate();
        if (latestNeedAggDate == null){
            ZLogger.df("POS设备最新的需要日结日期为空，不需要日结。");
            nextStep();
            return;
        }

        final String latestNeedAggDateStr = TimeCursor.FORMAT_YYYYMMDD.format(latestNeedAggDate);
        ZLogger.df(String.format("判断是否有日结未确认: %s", latestNeedAggDateStr));

        final DailysettleEntity dailysettleEntity = AnalysisHelper.createDailysettle(latestNeedAggDate);
        if (dailysettleEntity == null){
            ZLogger.df(String.format("创建日结单失败：%s", latestNeedAggDateStr));
            nextStep();
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null,
                    "网络未连接，暂停验证是否有日结未确认。");
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":false}
                        //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Boolean
                        //RspValue<Boolean> retValue = (RspValue<Boolean>) rspData;
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        if (StringUtils.isEmpty(retValue.getValue()) || retValue.getValue().equals("false")){
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_NO);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);

                            Bundle args = new Bundle();
                            args.putString("dailysettleDatetime", TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(latestNeedAggDate));
                            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_DAILYSETTLE,
                                    args,
                                    String.format("%s 未做日结，需要重新日结后才可以继续使用POS", latestNeedAggDateStr));
                        }
                        else{
                            ZLogger.df(String.format("%s已经日结", latestNeedAggDateStr));

                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_YES);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        ZLogger.df("判读是否日结失败：" + errMsg);
                        nextStep();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.analysisAccDateHaveDateEnd(latestNeedAggDate, responseCallback);
    }

    /**
     * 获取POS设备最新需要日结的日期，用于判断是否需要日结*/
    private Date getLatestNeedAggDate(){
        Date currentDate = new Date();

        //获取小于当前日期的最新一次已经完成的订单的更新日期
        String strWhere = String.format("updatedDate < '%s' and sellerId = '%d' and status = '%d'",
                TimeCursor.FORMAT_YYYYMMDD.format(currentDate), MfhLoginService.get().getSpid(),
                PosOrderEntity.ORDER_STATUS_FINISH);
        List<PosOrderEntity> orderEntityList = PosOrderService.get()
                .queryAllDesc(strWhere, new PageInfo(1, 10));
        if (orderEntityList != null && orderEntityList.size() > 1) {
            return orderEntityList.get(0).getUpdatedDate();
        }

        return null;
    }

    public class ValidateManagerEvent {
        public static final int EVENT_ID_VALIDATE_START             = 0X01;//验证开始
        public static final int EVENT_ID_VALIDATE_NEED_LOGIN        = 0X02;//需要登录
        public static final int EVENT_ID_VALIDATE_SESSION_EXPIRED   = 0X03;//会话过期
        public static final int EVENT_ID_VALIDATE_PLAT_NOT_REGISTER = 0X04;//设备未注册
        public static final int EVENT_ID_VALIDATE_NEED_DAILYSETTLE  = 0X05;//需要日结
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
