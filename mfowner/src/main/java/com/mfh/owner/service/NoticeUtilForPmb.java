package com.mfh.owner.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.manfenjiayuan.im.IMConstants;
import com.manfenjiayuan.im.database.entity.IMConversation;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.owner.ui.MainTabActivity;
import com.mfh.owner.ui.StartActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by 李潇阳 on 2014/9/25.
 */
public class NoticeUtilForPmb {

    public static final int NOTIFICATION_NEW_MESSAGE = 0;

    /**
     * 为了消息的解析，针对个推数据
     * @param data
     * @return
     */
    public static String paraseJosnForMsg(String data) {
        String content = "";
        try {
            JSONObject object = new JSONObject(data);
            JSONObject param = object.getJSONObject("param");
            JSONObject param1 = param.getJSONObject("param");
            content = param1.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        return content;
    }

    public static String getJsonVaLueByKey(String data, String theKey) {
        String response = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            Iterator iit = jsonObject.keys();
            while (iit.hasNext()) {
                String key = iit.next().toString();
                String value = jsonObject.getString(key);
                if (key.equals(theKey)) {
                    return value;
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return response;
    }

    /**
     * 显示提示框，notification
     * params i 0:消息，1：工单（或者是特殊服务）
     */
    public static void showNotification(final Activity that, int i, String title, String Content) {
        Notification notification = PushUtil.generateNotification(that, title, Content);

        Intent notificationIntent =new Intent(that, StartActivity.class); // 点击该通知后要跳转的Activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentItent = PendingIntent.getActivity(that, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PushUtil.showNotification(that, i, notification);
    }



    /**
     * 显示提示框，notification
     * params i 0:消息，1：工单（或者是特殊服务）
     */
    public static void showNotification(final Context context, int id, String title, String content) {
        Notification notification = PushUtil.generateNotification(context, title, content);
        notification.contentIntent = StartActivity.generatePendingIntent(context);

        PushUtil.showNotification(context, id, notification);
    }

    public static void showNotification(final Context context, int id, Long sid, String title, String content) {
        Notification notification = PushUtil.generateNotification(context, title, content);

        if(id == IMConstants.MSG_NOTIFICATION_SESSIOIN){
            Intent intent =new Intent(context, MainTabActivity.class); // 点击该通知后要跳转的Activity
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FILL_IN_DATA);
//            intent.setAction(String.valueOf(System.currentTimeMillis()));
            intent.setAction("MSG_NOTIFICATION_SESSIOIN");
            intent.putExtra(MainTabActivity.EXTRA_KEY_TAB_INDEX, 2);//2代表消息列表页面
            intent.putExtra("sessionId", sid);
            intent.putExtra("sessionId2", String.valueOf(sid));
            // Put an extra so we know when an activity launches if it is a from a notification
            intent.putExtra("Notification.EXTRA_HANDLING_NOTIFICATION", true);
            ZLogger.d(String.format("set notification,sessionId=%s(%s)", String.valueOf(sid), String.valueOf(sid)));

            notification.contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        //TODO
//        //新消息，点击跳转至对话页面
//        else if(id == MsgConstants.NOTIFICATION_NEW_MESSAGE){
//            notification.contentIntent = ChatActivity.generatePendingIntent(context, sid);
//        }
 else{
            notification.contentIntent = StartActivity.generatePendingIntent(context);
        }

        PushUtil.showNotification(context, Integer.valueOf(String.valueOf(sid)), notification);
    }

    public static void showNotificationWithSessionId(Context context,
                                                     IMConversation session, String title, String content) {
        Notification notification = PushUtil.generateNotification(context, title, content);

        //TODO
//        IMConversationService sessionService = ServiceFactory.getService(IMConversationService.class);
//        Intent notificationIntent =new Intent(context, ChatActivity.class); // 点击该通知后要跳转的Activity
//        //sessionService.getMessageSkipIntent(notificationIntent, session);
//        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        notificationIntent.addFlags(Intent.FILL_IN_DATA);
//        notificationIntent.putExtra("sessionId", session.getId());
//        notificationIntent.putExtra("humanName", session.getHumanname());
//        notificationIntent.putExtra("humanId", session.getHumanid());
//        notificationIntent.putExtra("msgMode", session);
//        notificationIntent.putExtra("headImgUrl", session.getLocalheadimageurl());
//        notificationIntent.putExtra("unReadCount", session.getUnreadcount());
//        notificationIntent.setAction(String.valueOf(System.currentTimeMillis()));
//        notification.contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        PushUtil.showNotification(context, Integer.valueOf(String.valueOf(session.getSessionid())), notification);
    }


}
