package com.mfh.litecashier.service;

import android.content.Context;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.hardware.printer.gprinter.GPrinter;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.igexin.sdk.message.FeedbackCmdMessage;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.igexin.sdk.message.SetTagCmdMessage;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.api.constant.Priv;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.event.AffairEvent;
import com.tencent.bugly.beta.Beta;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务,
 * 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class DemoIntentService extends GTIntentService {

    private static final String TAG = "DemoIntentService";

    /**
     * 为了观察透传数据变化.
     */
    private static int cnt;

    public DemoIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        ZLogger.d("个推 onReceiveServicePid -> " + pid);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
        String appid = msg.getAppid();
        String taskid = msg.getTaskId();
        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
        String pkg = msg.getPkgName();
        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
        ZLogger.d("call sendFeedbackMessage = " + (result ? "success" : "failed"));

        ZLogger.d("onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
                + "\ncid = " + cid);

        if (payload == null) {
            ZLogger.e("receiver payload = null");
        } else {
            String data = new String(payload);
            ZLogger.d("receiver payload = " + data);

            processPushPayload(context, data);
        }

        ZLogger.d("----------------------------------------------------------------------------------------------");
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        ZLogger.d("onReceiveClientId -> " + "clientid = " + clientid);

        if (clientid != null) {
            ZLogger.i2f(String.format("个推 clientId=%s-%s",
                    PushManager.getInstance().getClientid(CashierApp.getAppContext()),
                    clientid));
            IMConfig.savePushClientId(clientid);
        }

        /* 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
                以便以后通过用户帐号查找ClientID进行消息推送。有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，
                请应用程序在每次获取ClientID广播后，都能进行一次关联绑定 */
        //注册到消息桥
        IMClient.getInstance().registerBridge();
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        ZLogger.d("onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        ZLogger.d(TAG, "onReceiveCommandResult -> " + cmdMessage);

        int action = cmdMessage.getAction();

        if (action == PushConsts.SET_TAG_RESULT) {
            setTagResult((SetTagCmdMessage) cmdMessage);
        } else if ((action == PushConsts.THIRDPART_FEEDBACK)) {
            feedbackResult((FeedbackCmdMessage) cmdMessage);
        }
    }

    private void setTagResult(SetTagCmdMessage setTagCmdMsg) {
        String sn = setTagCmdMsg.getSn();
        String code = setTagCmdMsg.getCode();

        String text = "设置标签失败, 未知异常";
        switch (Integer.valueOf(code)) {
            case PushConsts.SETTAG_SUCCESS:
                text = "设置标签成功";
                break;

            case PushConsts.SETTAG_ERROR_COUNT:
                text = "设置标签失败, tag数量过大, 最大不能超过200个";
                break;

            case PushConsts.SETTAG_ERROR_FREQUENCY:
                text = "设置标签失败, 频率过快, 两次间隔应大于1s且一天只能成功调用一次";
                break;

            case PushConsts.SETTAG_ERROR_REPEAT:
                text = "设置标签失败, 标签重复";
                break;

            case PushConsts.SETTAG_ERROR_UNBIND:
                text = "设置标签失败, 服务未初始化成功";
                break;

            case PushConsts.SETTAG_ERROR_EXCEPTION:
                text = "设置标签失败, 未知异常";
                break;

            case PushConsts.SETTAG_ERROR_NULL:
                text = "设置标签失败, tag 为空";
                break;

            case PushConsts.SETTAG_NOTONLINE:
                text = "还未登陆成功";
                break;

            case PushConsts.SETTAG_IN_BLACKLIST:
                text = "该应用已经在黑名单中,请联系售后支持!";
                break;

            case PushConsts.SETTAG_NUM_EXCEED:
                text = "已存 tag 超过限制";
                break;

            default:
                break;
        }

        ZLogger.d(TAG, "settag result sn = " + sn + ", code = " + code + ", text = " + text);
    }

    private void feedbackResult(FeedbackCmdMessage feedbackCmdMsg) {
        String appid = feedbackCmdMsg.getAppid();
        String taskid = feedbackCmdMsg.getTaskId();
        String actionid = feedbackCmdMsg.getActionId();
        String result = feedbackCmdMsg.getResult();
        long timestamp = feedbackCmdMsg.getTimeStamp();
        String cid = feedbackCmdMsg.getClientId();

        ZLogger.d("onReceiveCommandResult -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nactionid = " + actionid + "\nresult = " + result
                + "\ncid = " + cid + "\ntimestamp = " + timestamp);
    }

    /**
     * 处理透传（payload）数据
     */
    private void processPushPayload(Context context, String data) {
        if (!MfhLoginService.get().haveLogined()) {
            ZLogger.d("用户未登录，忽略透传消息");
            return;
        }
        EmbMsg embMsg = EmbMsg.parseOjbect(data);
        if (embMsg == null) {
            ZLogger.d("透传消息解析失败");
            return;
        }

        //已经保存过的消息不再重复保存，客户端可能会收到多条重复的消息，只需要处理一条即可。
        EmbMsgService.getInstance().saveOrUpdate(embMsg, false);

        JSONObject msgBeanObj = JSONObject.parseObject(embMsg.getMsgBean());
        if (msgBeanObj == null) {
            return;
        }
        JSONObject bodyObj = msgBeanObj.getJSONObject("body");
        String content = bodyObj.getString("content");
        Integer bizType = msgBeanObj.getIntValue("bizType");//获取推送的数据类型
        String time = msgBeanObj.getString("time");
        ZLogger.d(String.format("<--%d\n%s\ncontent=%s", bizType, data, content));

        //SKU更新
        if (IMBizType.TENANT_SKU_UPDATE == bizType) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.TENANT_SKU_UPDATE);
            ZLogger.d(String.format("IMBizType.TENANT_SKU_UPDATE = %d", count));
            if (count > 0) {
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SKU));
            }
        } else if (IMBizType.FRONGCATEGORY_GOODS_UPDATE == bizType) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.FRONGCATEGORY_GOODS_UPDATE);
            ZLogger.d(String.format("IMBizType.FRONGCATEGORY_GOODS_UPDATE = %d", count));
            if (count > 0) {
                DataDownloadManager.get().sync(DataDownloadManager.FRONTENDCATEGORY_GOODS);
            }
        } else if (IMBizType.FRONTCATEGORY_UPDATE == bizType) {
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.FRONTCATEGORY_UPDATE);
            if (count > 0) {
                DataDownloadManager.get().sync(DataDownloadManager.FRONTENDCATEGORY | DataDownloadManager.FRONTENDCATEGORY_GOODS);
            }
        }
        //买手抢单组货
        else if (IMBizType.ORDER_TRANS_NOTIFY == bizType) {
//            MfhUserManager.getInstance().updateModules();
            if (!MfhUserManager.getInstance().containsModule(Priv.FUNC_SUPPORT_BUY)) {
                ZLogger.d("当前登录用户不是买手忽略订单消息");
                return;
            }
            int count = EmbMsgService.getInstance().getUnreadCount(IMBizType.ORDER_TRANS_NOTIFY);
            if (count > 0) {
                EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_ORDER_TRANS_NOTIFY));
            }
        }
        //新的采购订单
        else if (IMBizType.NEW_PURCHASE_ORDER == bizType) {
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER));
        }
        //现金超过授权额度，要求锁定pos机
        else if (IMBizType.LOCK_POS_CLIENT_NOTIFY == bizType) {
            Date createTime = TimeUtil.parse(time, TimeUtil.FORMAT_YYYYMMDDHHMMSS);
            Date rightNow = TimeUtil.getCurrentDate();

            //保留最近一个小时的消息
            if (createTime == null) {
                ZLogger.w("消息无效: time＝null");
                return;
            }

            Calendar msgTrigger = Calendar.getInstance();
            msgTrigger.setTime(createTime);

            Calendar minTrigger = Calendar.getInstance();
            minTrigger.add(Calendar.HOUR_OF_DAY, -1);
            if (minTrigger.after(msgTrigger)) {
                ZLogger.w(String.format("消息过期--当前时间:%s,消息创建时间:%s",
                        TimeUtil.format(rightNow, TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                        TimeUtil.format(createTime, TimeCursor.FORMAT_YYYYMMDDHHMMSS)));
                return;
            }

            ZLogger.i(String.format("现金超过授权额度--当前时间:%s,消息创建时间:%s",
                    TimeUtil.format(rightNow, TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                    TimeUtil.format(createTime, TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

            AlarmManagerHelper.triggleNextDailysettle(1);
            ValidateManager.get().stepValidate(ValidateManager.STEP_VALIDATE_CASHQUOTA);
        }
        //现金授权额度将要用完，即将锁定pos机
        else if (IMBizType.PRE_LOCK_POS_CLIENT_NOTIFY == bizType) {
            AlarmManagerHelper.triggleNextDailysettle(1);

            Double cashLimitAmount = Double.valueOf(content);
            ZLogger.d("cashQuotaAmount=" + cashLimitAmount);
            Bundle args = new Bundle();
            args.putDouble("amount", cashLimitAmount);

            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_PRE_LOCK_POS_CLIENT, args));
        } else if (IMBizType.REMOTE_CONTROL_CMD == bizType) {
            responseRemoteControl(JSONObject.parseObject(content));
        }
    }

    /**
     * 响应远程控制
     */
    private void responseRemoteControl(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        Long remoteId = jsonObject.getLong("remoteId");
        String remoteInfo = jsonObject.getString("remoteInfo");
        String remoteData = jsonObject.getString("data");
        ZLogger.i2f(String.format("<--远程控制: %d %s\n%s", remoteId, remoteInfo, remoteData));

        if (remoteId.equals(1L)) {
            RemoteControlClient.getInstance().remoteFeedback();
        } else if (remoteId.equals(2L)) {
            RemoteControlClient.getInstance().onekeyFeedback();
        } else if (remoteId.equals(3L)) {
            RemoteControlClient.getInstance().copyDatabase();
        } else if (remoteId.equals(4L)) {
            Beta.checkUpgrade(false, false);
        } else if (remoteId.equals(20L)) {
            GPrinter.print(remoteData);
        }
    }


//    case PushConsts.GET_SDKONLINESTATE:
//    //                Scheduling restart of crashed service com.mfh.litecashier/com.igexin.sdk.PushService in 1000ms
////                Start proc 16115:com.mfh.litecashier:pushservice/u0a96 for service com.mfh.litecashier/com.igexin.sdk.PushService
//    boolean onlineState = bundle.getBoolean("onlineState");
//    String clientId = PushManager.getInstance().getClientid(CashierApp.getAppContext());
//
//    ZLogger.df(String.format("个推 %s-%s, onlineState = %b",
//    clientId,
//            IMConfig.getPushClientId(), onlineState));
//    if (!StringUtils.isEmpty(clientId)){
//        if (!onlineState){
//            ZLogger.df("准备开启推送...");
//            PushManager.getInstance().turnOnPush(CashierApp.getAppContext());
//            offlineTime = System.currentTimeMillis();
//        }
//    }
//    else{
//        ZLogger.df("准备初始化个推服务...");
//        offlineTime = System.currentTimeMillis();
//        PushManager.getInstance().initialize(CashierApp.getAppContext(), DemoPushService.class);
//    }
//    break;
//    case PushConsts.GET_SDKSERVICEPID:
//    Long rightNow = System.currentTimeMillis();
//    Long interval =  rightNow - offlineTime;
//    ZLogger.df(String.format("个推服务未启动，%d - %d = %d", rightNow, offlineTime, interval));
////                超过5分钟会自动重启
//    if (interval > 10) {
//        ZLogger.df("准备初始化个推服务...");
//        offlineTime = System.currentTimeMillis();
//        PushManager.getInstance().initialize(CashierApp.getAppContext(), DemoPushService.class);
//    }
//    break;
}
