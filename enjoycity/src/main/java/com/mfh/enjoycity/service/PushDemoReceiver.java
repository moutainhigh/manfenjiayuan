package com.mfh.enjoycity.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.igexin.sdk.PushConsts;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;

/**
 * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息
 * Created by Nat.ZZN(bingshanguxue) on 2015/08/17.
 */
public class PushDemoReceiver extends BroadcastReceiver {

    private static final String KEY_CLIENT_ID   = "clientid";
    private static final String KEY_PAYLOAD     = "payload";
    private static final String KEY_APPID       = "appid";


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        ZLogger.d(String.format("PushDemoReceive.onReceive, action=%d", bundle.getInt("action")));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:{
                // 获取透传数据
                // String appid = bundle.getString(KEY_APPID);
                parsePushPayload(context, bundle.getByteArray(KEY_PAYLOAD));
            }
                break;
            case PushConsts.GET_CLIENTID:{
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String clientId = bundle.getString(KEY_CLIENT_ID);
                if(clientId != null){
                    ZLogger.d(String.format("PushDemoReceive.enjoycity.clientId=%s" ,clientId));
                    IMConfig.savePushClientId(clientId);
                }

                //注册到消息桥
                IMClient.getInstance().registerBridge();
//                com.mfh.comna.api.helper.UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_PARTER_REFRESH);
            }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;

            case PushConsts.GET_SDKSERVICEPID:
                // 重新初始化sdk
//                PushManager.getInstance().initialize(AppContext.getAppContext());
                break;
            default:
                break;
        }
    }

    /**
     * 解析透传数据
     * */
    public static void parsePushPayload(Context context, byte[] payload){
        if(payload == null){
            return;
        }

        String data = new String(payload);
        String bizType = PushUtil.getJsonMsgType(data);//获取推送的数据类型
        ZLogger.d(String.format("PushDemoReceive.payload=(%s)%s", bizType, data));

        //小伙伴
        if (String.valueOf(IMBizType.NOTIFY).equals(bizType)){

            Bundle extras = new Bundle();

            JSONObject jsonObject = JSONObject.parseObject(data);
            JSONObject msgObj = jsonObject.getJSONObject("msg");
            JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
            JSONObject bodyObj = msgBeanObj.getJSONObject("body");
            JSONObject metaObj = msgObj.getJSONObject("meta");
            if (metaObj != null){
                extras.putString("orderId", metaObj.getString("tagOne"));
            }
            if (bodyObj != null){

                extras.putString("content", bodyObj.getString("content"));

                //程序退到后台，显示通知
                if(MfhLoginService.get().haveLogined() && SharedPrefesManagerFactory.getNotificationAcceptEnabled()){// && !PushUtil.isForeground(context)){
//                    com.mfh.comna.api.helper.UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_NOTIFY_TAKE_ORDER, extras);
//                    Notification notification = PushUtil.generateNotification(context, "接单通知", bodyObj.getString("content"));
//                    PushUtil.showNotification(context, 0, notification);
                }
            }
        }
        else if (String.valueOf(IMBizType.EVALUATE_ORDER).equals(bizType)){

            Bundle extras = new Bundle();

            JSONObject jsonObject = JSONObject.parseObject(data);
            JSONObject msgObj = jsonObject.getJSONObject("msg");
            JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
            JSONObject bodyObj = msgBeanObj.getJSONObject("body");
            JSONObject metaObj = msgObj.getJSONObject("meta");
            if (metaObj != null){
                extras.putString("orderIds", metaObj.getString("tagOne"));
            }
            if (bodyObj != null){
                extras.putString("content", bodyObj.getString("content"));

                if (MfhLoginService.get().haveLogined()){
//                    com.mfh.comna.api.helper.UIHelper.sendBroadcast(Constants.BROADCAST_ACTION_NOTIFY_EVALUATE_ORDER, extras);
//                    Notification notification = PushUtil.generateNotification(context, "送达", bodyObj.getString("content"));
//                    PushUtil.showNotification(context, 1, notification);
                }
            }
        }
//        else if (String.valueOf(IMBizType.ASK).equals(bizType)
//                || String.valueOf(IMBizType.CHAT).equals(bizType)
//                || String.valueOf(IMBizType.CS).equals(bizType)) {
//
//            //刷新会话列表
//            IMConversationService sessionService = ServiceFactory.getService(IMConversationService.class);
//            sessionService.saveNewSession(data);
//
//            //保存消息
//            EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class);
//            String id = msgService.saveNewMsg(data);
//
//            EmbMsg msg = msgService.getDao().getEntityById(id);
//
//            //程序退到后台，显示通知
//            if(SharedPreferencesHelper.getNotificationAcceptEnabled() && !PushUtil.isForeground(context)){
//                //TODO
////                Intent intent1 = new Intent(MsgConstants.ACTION_RECEIVE_MSG_BACK);
////                context.sendBroadcast(intent1);
//
//                showNotification(context, msg);
//            }
//        }
    }

//    /**
//     * 显示通知:新消息
//     * */
//    public static void showNotification(Context context, EmbMsg msg){
//        if(msg == null){
//            return;
//        }
//
//        String content = "";
//        String msgInfo = msg.getMsgInfo();
//        //ensure json string
//        if (msgInfo!=null && msgInfo.startsWith("{")){
//            WxParam msgParam = WxParam.fromJson(msgInfo);
//            if (msgParam.getContent() != null){
//                content = msgParam.getContent();
//            }
//
//            if (msgParam instanceof ImageParam) {
//                ImageParam imgParam = (ImageParam)msgParam;
//                final String picUrl = imgParam.getPicurl();
//                if(!com.mfh.comna.comn.utils.StringUtils.isEmpty(picUrl)){
//                    content = picUrl;
//                }
//            }
//        }
//
//        Long sessionId = msg.getSessionid();
//        String createdBy = msg.getCreatedBy();
//        if(sessionId != null && !TextUtils.isEmpty(createdBy) && !TextUtils.isEmpty(content)){
//            Notification notification = PushUtil.generateNotification(context, createdBy, content);
//            notification.contentIntent = ChatActivity.generatePendingIntent(context, sessionId);
//            PushUtil.showNotification(context, Integer.valueOf(String.valueOf(sessionId)), notification);
//        }
//    }

}
