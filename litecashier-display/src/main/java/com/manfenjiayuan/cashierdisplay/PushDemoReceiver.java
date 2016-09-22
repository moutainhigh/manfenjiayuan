package com.manfenjiayuan.cashierdisplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.igexin.sdk.PushConsts;
import com.manfenjiayuan.cashierdisplay.bean.CashierOrderInfoWrapper;
import com.manfenjiayuan.im.IMClient;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.bean.MsgBean;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.constants.IMTechType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.helper.PayloadHelper;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;

import de.greenrobot.event.EventBus;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * Created by Nat.ZZN(bingshanguxue) on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID = "clientid";
    private static final String KEY_PAYLOAD = "payload";
    private static final String KEY_APPID = "appid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ZLogger.df(String.format("onReceive, action=%d", bundle.getInt("action")));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA: {
                // 获取透传（payload）数据
                byte[] payload = bundle.getByteArray(KEY_PAYLOAD);
                if (payload != null) {
                    String data = new String(payload);
                    parsePushPayload(context, data);
                }
                // String appid = bundle.getString(KEY_APPID);
            }
            break;
            case PushConsts.GET_CLIENTID: {
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if (clientId != null) {
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
                ZLogger.d(String.format("sdk.online.clientId=%s", IMConfig.getPushClientId()));
                break;
            case PushConsts.GET_SDKSERVICEPID:
                // 重新初始化sdk
//                ZLogger.d("initializing getui sdk...");
//                PushManager.getInstance().initialize(CashierApp.getAppContext());
                break;
            default:
                break;
        }
    }

    /**
     * 处理透传（payload）数据
     */
    private static void parsePushPayload(Context context, String data) {
        int bizType = PayloadHelper.getJsonMsgType(data);//获取推送的数据类型
        ZLogger.df(String.format("payload=(%s)%s", bizType, data));

        //小伙伴
        if (IMBizType.ORDER_TRANS_NOTIFY == bizType) {
            Bundle extras = new Bundle();

            JSONObject jsonObject = JSONObject.parseObject(data);
            JSONObject msgObj = jsonObject.getJSONObject("msg");
            JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
            JSONObject bodyObj = msgBeanObj.getJSONObject("body");
            JSONObject metaObj = msgObj.getJSONObject("meta");
            if (metaObj != null) {
                extras.putString("orderId", metaObj.getString("tagOne"));
            }
            if (bodyObj != null) {
                extras.putString("content", bodyObj.getString("content"));

//                EventBus.getDefault().post(
//                        new AffairEvent(AffairEvent.EVENT_ID_APPEND_UNREAD_ORDER));

                //程序退到后台，显示通知
                if (MfhLoginService.get().haveLogined() && SharedPreferencesManager.getNotificationAcceptEnabled()) {// && !PushUtil.isForeground(context)){
//                    UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER, extras);
//                    Notification notification = PushUtil.generateNotification(context, "接单通知", bodyObj.getString("content"));
//                    PushUtil.showNotification(context, 0, notification);
                }
            }
        } else if (IMBizType.CUSTOMER_DISPLAY_PAYORDER == bizType){
//            DialogUtil.showHint(data);
            try {
                EmbMsg embMsg = EmbMsg.parseOjbect(JSONObject.parseObject(data));
                MsgBean msgBean = JSONObject.parseObject(embMsg.getParam(), MsgBean.class);
                ZLogger.df(String.format("bizType=(%d)", msgBean.getBizType()));

                String body = JSON.toJSONString(msgBean.getBody());
                if (IMTechType.TEXT.equals(msgBean.getType())) {
                    TextParam textParam = TextParam.fromJson(body);
                    CashierOrderInfoWrapper cashierOrderInfoWrapper = JSON.toJavaObject(JSONObject.parseObject(textParam.getContent()), CashierOrderInfoWrapper.class);
                    ZLogger.df(String.format("cashierOrderInfo=%s", JSON.toJSONString(cashierOrderInfoWrapper)));

                    EventBus.getDefault().post(new CashierOrderEvent(cashierOrderInfoWrapper));
                }
                //TODO
                else {
                    ZLogger.df(String.format("body=(%s)", body));
                }

            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }
}
