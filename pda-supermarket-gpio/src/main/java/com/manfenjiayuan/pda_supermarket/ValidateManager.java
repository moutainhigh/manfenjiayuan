package com.manfenjiayuan.pda_supermarket;


import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.IMConfig;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.invOrder.MfhApiImpl;
import com.mfh.framework.api.invOrder.UserApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

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
    public static final int STEP_REGISTER_PLAT = 1;// 检查是否已经登录/会话是否有效

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
            ZLogger.df("Validate--正在验证数据...");
            return;
        }

        processStep(STEP_VALIDATE_SESSION, STEP_REGISTER_PLAT);
    }

    /**
     * 单步验证*/
    public void stepValidate(int step) {
        if (bSyncInProgress) {
            ZLogger.df("Validate--正在验证数据...");
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
                if (StringUtils.isEmpty(SharedPreferencesManager.getTerminalId())){
                    registerPlat();
                }
                else{
                    nextStep();
                }
            }
            break;
            default: {
                validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_FINISHED, null, "Validate--验证结束");
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
            if (NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
                checkSessionExpire();
            } else {
                nextStep();
//                //没有网络，跳过验证会话过期，直接进行下一步操作
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_NOT_LOGIN, null, "Validate--未登录，跳转到登录页面");
//                    }
//                }, 1000);
            }
        }
        //未登录
        else {
            validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_NEED_LOGIN, null, "Validate--未登录，跳转到登录页面");
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
                            ZLogger.df("Validate--注册设备成功:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                            nextStep();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("Validate--注册设备失败,%s", errMsg));

                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_PLAT_NOT_REGISTER,
                                null, "Validate--设备注册失败，需要重新注册");
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("serialNo", MfhApplication.getWifiMac15Bit());
        order.put("channelId", MfhApi.CHANNEL_ID);
        order.put("channelPointId", IMConfig.getPushClientId());
        order.put("netId", MfhLoginService.get().getCurOfficeId());

        ZLogger.df("Validate--注册设备中..." + order.toJSONString());
        MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
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
                        ZLogger.df("Validate--登录状态有效 ");
                        nextStep();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("Validate--" + errMsg);
                        validateFinished(ValidateManagerEvent.EVENT_ID_VALIDATE_SESSION_EXPIRED,
                                null, "Validate--会话过期，自动重登录");
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        UserApiImpl.validSession(responseCallback);
    }


    public class ValidateManagerEvent {
        public static final int EVENT_ID_VALIDATE_START             = 0X01;//验证开始
        public static final int EVENT_ID_VALIDATE_NEED_LOGIN        = 0X02;//需要登录
        public static final int EVENT_ID_VALIDATE_SESSION_EXPIRED   = 0X03;//会话过期
        public static final int EVENT_ID_VALIDATE_PLAT_NOT_REGISTER = 0X04;//设备未注册
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
