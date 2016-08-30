package com.mfh.litecashier.service;

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
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * Created by bingshanguxue on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID   = "clientid";
    private static final String KEY_PAYLOAD     = "payload";
    private static final String KEY_APPID       = "appid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ZLogger.df(String.format("bundle=%s", StringUtils.decodeBundle(bundle)));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:{
                // 获取透传（payload）数据
                try{
                    byte[] payload = bundle.getByteArray(KEY_PAYLOAD);
                    if (payload != null){
                        String data = new String(payload);
                        parsePushPayload(context, data);
                    }
                }
                catch (Exception e){
                    ZLogger.ef(e.toString());
                }
            }
                break;
            case PushConsts.GET_CLIENTID:{
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if(clientId != null){
                    ZLogger.df(String.format("clientId=%s", clientId));
                    IMConfig.savePushClientId(clientId);
                }

                // TODO:
                /* 第三方应用需要将ClientID上传到第三方服务器，并且将当前用户帐号和ClientID进行关联，
                以便以后通过用户帐号查找ClientID进行消息推送。有些情况下ClientID可能会发生变化，为保证获取最新的ClientID，
                请应用程序在每次获取ClientID广播后，都能进行一次关联绑定 */
                //注册到消息桥
                IMClient.getInstance().registerBridge();
//                UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_PARTER_REFRESH);
            }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;

            case PushConsts.GET_SDKONLINESTATE:
                ZLogger.df("sdk is online");
                break;
            case PushConsts.GET_SDKSERVICEPID:
                // 重新初始化sdk
//                ZLogger.d("initializing getui sdk...");
                PushManager.getInstance().initialize(CashierApp.getAppContext());
                break;
            default:
                break;
        }
    }

    /**
     * 处理透传（payload）数据
     * */
    private static void parsePushPayload(Context context, String data){
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONObject msgObj = jsonObject.getJSONObject("msg");
        JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");

        if (msgBeanObj == null){
            return;
        }

        JSONObject bodyObj = msgBeanObj.getJSONObject("body");
        Integer bizType = msgBeanObj.getIntValue("bizType");//获取推送的数据类型
        String time = msgBeanObj.getString("time");
        ZLogger.df(String.format("%s--%s", bizType, data));

        //小伙伴
        if (IMBizType.ORDER_TRANS_NOTIFY == bizType){
            Bundle extras = new Bundle();
            JSONObject metaObj = msgObj.getJSONObject("meta");
            if (metaObj != null){
                extras.putString("orderId", metaObj.getString("tagOne"));
            }
            if (bodyObj != null){
                extras.putString("content", bodyObj.getString("content"));

                EventBus.getDefault().post(
                        new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_ORDER));

                //程序退到后台，显示通知
                if(MfhLoginService.get().haveLogined() && SharedPreferencesManager.getNotificationAcceptEnabled()){// && !PushUtil.isForeground(context)){
//                    UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER, extras);
//                    Notification notification = PushUtil.generateNotification(context, "接单通知", bodyObj.getString("content"));
//                    PushUtil.showNotification(context, 0, notification);
                }
            }
        }
        //SKU更新
        else if (IMBizType.TENANT_SKU_UPDATE == bizType){
            Date createTime = TimeUtil.parse(time, TimeUtil.FORMAT_YYYYMMDDHHMMSS);

            //保留最近一个小时的消息
            if (createTime == null){
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

            int count = SharedPreferencesHelper
                    .getInt(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, 0);
            SharedPreferencesHelper
                    .set(SharedPreferencesHelper.PK_SKU_UPDATE_UNREADNUMBER, count+1);
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SKU));
        }
        //新的采购订单
        else if (IMBizType.NEW_PURCHASE_ORDER == bizType){
            int count = SharedPreferencesHelper
                    .getInt(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, 0);
            SharedPreferencesHelper
                    .set(SharedPreferencesHelper.PK_ONLINE_FRESHORDER_UNREADNUMBER, count+1);

            //同步数据
            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SCHEDULE_ORDER));
        }
        //现金超过授权额度，要求锁定pos机
        else if (IMBizType.LOCK_POS_CLIENT_NOTIFY == bizType){
            Date createTime = TimeUtil.parse(time, TimeUtil.FORMAT_YYYYMMDDHHMMSS);

            //保留最近一个小时的消息
            if (createTime == null){
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

            ZLogger.df(String.format("现金超过授权额度--当前时间:%s,消息创建时间:%s",
                    TimeUtil.format(new Date(), TimeCursor.FORMAT_YYYYMMDDHHMMSS),
                    TimeUtil.format(createTime, TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

            AlarmManagerHelper.triggleNextDailysettle(1);
            ValidateManager.get().stepValidate(ValidateManager.STEP_VALIDATE_CASHQUOTA);
        }
        //现金授权额度将要用完，即将锁定pos机
        else if (IMBizType.PRE_LOCK_POS_CLIENT_NOTIFY == bizType){
            AlarmManagerHelper.triggleNextDailysettle(1);

            String content = bodyObj.getString("content");

            Double cashLimitAmount = Double.valueOf(content);
            ZLogger.d("cashQuotaAmount=" + cashLimitAmount);
            Bundle args = new Bundle();
            args.putDouble("amount", cashLimitAmount);

            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_PRE_LOCK_POS_CLIENT, args));
        }
    }
}
