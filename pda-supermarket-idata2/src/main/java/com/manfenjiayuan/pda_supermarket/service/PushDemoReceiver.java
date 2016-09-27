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
import com.manfenjiayuan.pda_supermarket.event.AffairEvent;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;

import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * Created by Nat.ZZN(bingshanguxue) on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID = "clientid";
    private static final String KEY_PAYLOAD = "payload";
    private static final String KEY_APPID = "appid";

    private long offlineTime = System.currentTimeMillis();


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String processName = AppContext.getProcessName(AppContext.getAppContext(),
                android.os.Process.myPid());
        ZLogger.df(String.format("个推<%s> -- bundle=%s", processName,
                StringUtils.decodeBundle(bundle)));

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA: {
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
            case PushConsts.GET_CLIENTID: {
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if (clientId != null) {
                    ZLogger.df(String.format("个推 clientId=%s", clientId));
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
                break;

            case PushConsts.GET_SDKONLINESTATE:
                boolean onlineState = bundle.getBoolean("onlineState");
                String clientId = PushManager.getInstance().getClientid(AppContext.getAppContext());
                ZLogger.df(String.format("个推 %s-%s, onlineState = %b",
                        clientId, IMConfig.getPushClientId(), onlineState));
                if (!StringUtils.isEmpty(clientId)) {
                    if (!onlineState) {
                        ZLogger.df("准备开启推送...");
                        PushManager.getInstance().turnOnPush(AppContext.getAppContext());
                        offlineTime = System.currentTimeMillis();
                    }
                } else {
                    ZLogger.df("准备初始化个推服务...");
                    offlineTime = System.currentTimeMillis();
                    PushManager.getInstance().initialize(AppContext.getAppContext());
                }
                break;
            case PushConsts.GET_SDKSERVICEPID:
//                超过5分钟会自动重启
                Long rightNow = System.currentTimeMillis();
                Long interval = rightNow - offlineTime;
                ZLogger.df(String.format("个推服务未启动，%d - %d = %d", rightNow, offlineTime, interval));
//                超过5分钟会自动重启
                if (interval > 10) {
                    ZLogger.df("准备初始化个推服务...");
                    offlineTime = System.currentTimeMillis();
                    PushManager.getInstance().initialize(AppContext.getAppContext());
                }
                break;
            default:
                break;
        }
    }

    /**
     * 处理透传（payload）数据
     */
    private static void parsePushPayload(Context context, String data) {
        EmbMsg embMsg = EmbMsg.parseOjbect(data);
        if (embMsg == null){
            return;
        }

        //已经保存过的消息不再重复保存，客户端可能会收到多条重复的消息，只需要处理一条即可。
        EmbMsgService.getInstance().saveOrUpdate(embMsg, false);

//        JSONObject jsonObject = JSONObject.parseObject(data);
//        JSONObject msgObj = jsonObject.getJSONObject("msg");
//        JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
        JSONObject msgBeanObj = JSONObject.parseObject(embMsg.getMsgBean());
        if (msgBeanObj == null){
            return;
        }
        JSONObject bodyObj = msgBeanObj.getJSONObject("body");
        String content = bodyObj.getString("content");
        Integer bizType = msgBeanObj.getIntValue("bizType");//获取推送的数据类型
        String time = msgBeanObj.getString("time");
        ZLogger.df(String.format("<--%s\n%s\ncontent=%s", bizType, data, content));

        //SKU更新
        if (IMBizType.TENANT_SKU_UPDATE == bizType){
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

            EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_SKU));
        }
    }
}
