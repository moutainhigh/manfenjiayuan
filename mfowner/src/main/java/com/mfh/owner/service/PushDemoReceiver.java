package com.mfh.owner.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.igexin.sdk.PushConsts;
import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.IMConfig;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.im.database.service.IMConversationService;
import com.manfenjiayuan.im.param.ImageParam;
import com.manfenjiayuan.im.param.WxParam;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

/**
 * 个推启动与接受数据
 * Created by 李潇阳 on 2014/11/17.
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
                    ZLogger.d(String.format("PushDemoReceive.clientId=%s" ,clientId));
                    IMConfig.savePushClientId(clientId);
                }
            }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
                break;

            default:
                break;
        }
    }

    /**
     * 解析推送消息
     * */
    public static void parsePushPayload(Context context, byte[] payload){
        if(payload == null){
            return;
        }

        String data = new String(payload);
        String bizType = PushUtil.getJsonMsgType(data);//获取推送的数据类型
        ZLogger.d(String.format("PushDemoReceive.payload=(%s)%s", bizType, data));

        if (String.valueOf(IMBizType.ASK).equals(bizType)
                || String.valueOf(IMBizType.CHAT).equals(bizType)
                || String.valueOf(IMBizType.CS).equals(bizType)) {

            //刷新会话列表
            IMConversationService sessionService = ServiceFactory.getService(IMConversationService.class);
            sessionService.saveNewSession(data);

            //保存消息
            EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class);
            String id = msgService.saveNewMsg(data);

            EmbMsg msg = msgService.getDao().getEntityById(id);

            //程序退到后台，显示通知
            if(SharedPrefesManagerFactory.getNotificationAcceptEnabled() && !PushUtil.isForeground(context)){
                //TODO
//                Intent intent1 = new Intent(MsgConstants.ACTION_RECEIVE_MSG_BACK);
//                context.sendBroadcast(intent1);

                showNotification(context, msg);
            }
        }
    }

    /**
     * 显示通知:新消息
     * */
    public static void showNotification(Context context, EmbMsg msg){
        if(msg == null){
            return;
        }

        String content = "";
        String msgInfo = msg.getMsgInfo();
        //ensure json string
        if (msgInfo!=null && msgInfo.startsWith("{")){
            WxParam msgParam = WxParam.fromJson(msgInfo);
            if (msgParam.getContent() != null){
                content = msgParam.getContent();
            }

            if (msgParam instanceof ImageParam) {
                ImageParam imgParam = (ImageParam)msgParam;
                final String picUrl = imgParam.getPicurl();
                if(!StringUtils.isEmpty(picUrl)){
                    content = picUrl;
                }
            }
        }

        Long sessionId = msg.getSessionid();
        String createdBy = msg.getCreatedBy();
        if(sessionId != null && !TextUtils.isEmpty(createdBy) && !TextUtils.isEmpty(content)){
            //TODO
//            Notification notification = PushUtil.generateNotification(context, createdBy, content);
//            notification.contentIntent = ChatActivity.generatePendingIntent(context, sessionId);
//            PushUtilushUtil.showNotification(context, Integer.valueOf(String.valueOf(sessionId)), notification);
        }
    }

}
