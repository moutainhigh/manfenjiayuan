package com.mfh.enjoycity.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mfh.enjoycity.ui.activity.StartActivity;

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
}
