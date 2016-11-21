package com.manfenjiayuan.pda_supermarket.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.event.AffairEvent;
import com.manfenjiayuan.pda_supermarket.ui.MainActivity;
import com.mfh.framework.core.utils.NotificationUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.anlaysis.remoteControl.RemoteControlClient;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.tencent.bugly.beta.Beta;

import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * <ol>
 * ClientID在哪些情况下会改变？
 * <li>用户超过三个月未登录，之后再登录会重新生成一个CID</li>
 * <li>双清：即卸载应用，清除Sdcard下libs文件夹，然后重新安装（只适用与Android客户端）</li>
 * <li>Android：应用的包名修改；iOS：bundleID的修改（越狱手机卸载安装也有可能会变）</li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID = "clientid";
    private static final String KEY_PAYLOAD = "payload";
    private static final String KEY_APPID = "appid";
    private static final String KEY_TASKID = "taskid";
    private static final String KEY_MESSAGEID = "messageid";

    private long offlineTime = System.currentTimeMillis();


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ZLogger.df(String.format("个推:%s\n%s",
                retrieveGetuiInfo(AppContext.getAppContext()),
                StringUtils.decodeBundle(bundle)));

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA: {
                ZLogger.df(String.format("action: %d , %s",
                        PushConsts.GET_MSG_DATA, retrieveGetuiInfo(AppContext.getAppContext())));

                    // 获取透传（payload）数据
                try {
                    byte[] payload = bundle.getByteArray(KEY_PAYLOAD);
                    String taskid = bundle.getString(KEY_TASKID);
                    String messageid = bundle.getString(KEY_MESSAGEID);

                    if (payload != null) {
                        String data = new String(payload);
                        parsePushPayload(context, data);
                    }
                } catch (Exception e) {
                    ZLogger.ef(e.toString());
                }
            }
            break;
            case PushConsts.GET_CLIENTID: {
                ZLogger.df(String.format("action: %d, %s",
                        PushConsts.GET_CLIENTID, retrieveGetuiInfo(AppContext.getAppContext())));

                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if (clientId != null) {
                    IMConfig.savePushClientId(clientId);
                }

                // TODO:
                /* 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
                以便以后通过用户帐号查找ClientID进行消息推送。有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，
                请应用程序在每次获取ClientID广播后，都能进行一次关联绑定 */
                //注册到消息桥
                IMClient.getInstance().registerBridge();
            }
            break;
            case PushConsts.THIRDPART_FEEDBACK:
                ZLogger.df(String.format("action:%d, %s",
                        PushConsts.THIRDPART_FEEDBACK, retrieveGetuiInfo(AppContext.getAppContext())));
                break;
            case PushConsts.GET_SDKONLINESTATE: {
                ZLogger.df(String.format("action:%d, %s",
                        PushConsts.GET_SDKONLINESTATE, retrieveGetuiInfo(AppContext.getAppContext())));

                boolean onlineState = bundle.getBoolean("onlineState");
                String clientId = PushManager.getInstance().getClientid(AppContext.getAppContext());
                ZLogger.df(String.format("个推 onlineState = %b", onlineState));
                if (!StringUtils.isEmpty(clientId)) {
                    if (!onlineState) {
                        ZLogger.df("准备开启推送...");
                        PushManager.getInstance().turnOnPush(AppContext.getAppContext());
                        offlineTime = System.currentTimeMillis();
                    } else {

                    }
                } else {
                    ZLogger.df("准备初始化个推服务...");
                    offlineTime = System.currentTimeMillis();
                    PushManager.getInstance().initialize(AppContext.getAppContext());
                }
            }

            break;
            case PushConsts.GET_SDKSERVICEPID: {
                ZLogger.df(String.format("action:%d, %s",
                        PushConsts.GET_SDKSERVICEPID, retrieveGetuiInfo(AppContext.getAppContext())));

//                超过5分钟会自动重启
                Long rightNow = System.currentTimeMillis();
                Long interval = rightNow - offlineTime;
                String cid = PushManager.getInstance().getClientid(AppContext.getAppContext());

                ZLogger.df(String.format("个推服务未启动，%d - %d = %d",
                        rightNow, offlineTime, interval));
                if (PushManager.getInstance().isPushTurnedOn(AppContext.getAppContext())) {
                    ZLogger.d("个推服务已经开启");
//                    PushManager.getInstance().stopService(AppContext.getAppContext());
                    PushManager.getInstance().initialize(AppContext.getAppContext());
                } else {
                    ZLogger.d("个推服务未开启");
                    offlineTime = System.currentTimeMillis();

                    if (StringUtils.isEmpty(cid)) {
                        ZLogger.df("准备初始化个推服务...");
                        PushManager.getInstance().initialize(AppContext.getAppContext());
//                        PushManager.getInstance().turnOnPush(AppContext.getAppContext());
                    } else {
                        ZLogger.df("准备开启个推服务...");
                        PushManager.getInstance().turnOnPush(AppContext.getAppContext());
                    }
                }
            }
            break;
            default: {
                ZLogger.df(retrieveGetuiInfo(AppContext.getAppContext()));
            }
            break;
        }
    }

    /**
     * 个推服务信息
     */
    private String retrieveGetuiInfo(Context context) {
        if (context == null) {
            return null;
        }
        String version = PushManager.getInstance().getVersion(context);
        String cid = PushManager.getInstance().getClientid(context);
        boolean onlineState = PushManager.getInstance().isPushTurnedOn(context);
        return String.format("version=%s, cid=(%s/%s), onlineState = %b",
                version, cid, IMConfig.getPushClientId(), onlineState);
    }

    /**
     * 处理透传（payload）数据
     */
    private void parsePushPayload(Context context, String data) {
        if (StringUtils.isEmpty(data)) {
            return;
        }

        ZLogger.d(data);
        EmbMsg embMsg = EmbMsg.parseOjbect(data);
        if (embMsg == null) {
            return;
        }

        //已经保存过的消息不再重复保存，客户端可能会收到多条重复的消息，只需要处理一条即可。
        EmbMsgService.getInstance().saveOrUpdate(embMsg, false);

//        JSONObject jsonObject = JSONObject.parseObject(data);
//        JSONObject msgObj = jsonObject.getJSONObject("msg");
//        JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
        JSONObject msgBeanObj = JSONObject.parseObject(embMsg.getMsgBean());
        if (msgBeanObj == null) {
            return;
        }
        JSONObject bodyObj = msgBeanObj.getJSONObject("body");
        String content = bodyObj.getString("content");
        Integer bizType = msgBeanObj.getIntValue("bizType");//获取推送的数据类型
        String time = msgBeanObj.getString("time");
        ZLogger.df(String.format("<--%s\n%s\ncontent=%s", bizType, data, content));

        //SKU更新
        if (IMBizType.TENANT_SKU_UPDATE == bizType) {
            Date createTime = TimeUtil.parse(time, TimeUtil.FORMAT_YYYYMMDDHHMMSS);

            //保留最近一个小时的消息
            if (createTime == null) {
                ZLogger.df("消息无效: time＝null");
                return;
            }

            Calendar msgTrigger = Calendar.getInstance();
            msgTrigger.setTime(createTime);

            Calendar minTrigger = Calendar.getInstance();
            minTrigger.add(Calendar.HOUR_OF_DAY, -1);
            if (minTrigger.after(msgTrigger)) {
                ZLogger.df(String.format("消息过期--当前时间:%s,消息创建时间:%s",
                        TimeUtil.format(new Date(), TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                        TimeUtil.format(createTime, TimeCursor.FORMAT_YYYYMMDDHHMMSS)));
                return;
            }

            ZLogger.df(String.format("SKU更新--当前时间:%s,消息创建时间:%s",
                    TimeUtil.format(new Date(), TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                    TimeUtil.format(createTime, TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SKU));
        }
        //买手抢单组货
        else if (IMBizType.ORDER_TRANS_NOTIFY == bizType) {
            Bundle extras = new Bundle();

            JSONObject jsonObject = JSONObject.parseObject(data);
            JSONObject msgObj = jsonObject.getJSONObject("msg");
            JSONObject metaObj = msgObj.getJSONObject("meta");
            if (metaObj != null) {
                extras.putString("orderId", metaObj.getString("tagOne"));
            }
            extras.putString("content", content);

            NotificationUtils.makeNotification(AppContext.getAppContext(), R.mipmap.ic_launcher,
                    "米西厨房PDA", "新订单", MainActivity.class);
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_BUYER_PREPAREABLE));

            //程序退到后台，显示通知
            if (MfhLoginService.get().haveLogined() && SharedPrefesManagerFactory.getNotificationAcceptEnabled()) {
                // && !PushUtil.isForeground(context)){
//                    UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER, extras);
//                    Notification notification = PushUtil.generateNotification(context, "接单通知", bodyObj.getString("content"));
//                    PushUtil.showNotification(context, 0, notification);
                // TODO: 24/10/2016 红点提示
            }
        } else if (IMBizType.REMOTE_CONTROL_CMD == bizType) {
            responseRemoteControl(JSONObject.parseObject(content));
        }
    }

    /**
     * 响应远程控制
     * */
    private void responseRemoteControl(JSONObject jsonObject){
        if (jsonObject == null){
            return;
        }
        Long remoteId = jsonObject.getLong("remoteId");
        String remoteInfo = jsonObject.getString("remoteInfo");
        String remoteData = jsonObject.getString("data");
        ZLogger.df(String.format("<--远程控制: %d %s\n%s", remoteId, remoteInfo, remoteData));

        if (remoteId.equals(1L)) {
            RemoteControlClient.getInstance().uploadLogFileStep1();
        } else if (remoteId.equals(2L)) {
            RemoteControlClient.getInstance().uploadCrashFileStep1();
        } else if (remoteId.equals(3L)) {
            Beta.checkUpgrade(false, false);
        }
    }

}
