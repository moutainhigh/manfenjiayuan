package com.mfh.buyers.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 接收推送数据的工具类
 * Created by 李潇阳 on 2014/9/4.
 */
public class PushUtil {

    /**
     * generate new notificatioin
     * */
    public static Notification generateNotification(Context context, String title, String text){
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(com.mfh.comna.R.drawable.colour_logo)
                .setAutoCancel(true)
                .setWhen(com.mfh.comna.comn.utils.TimeUtil.genTimeStamp())
                .build();
        //设定Notification出现时的声音
        notification.defaults |= Notification.DEFAULT_SOUND;
        //设定如何振动
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //notification.ledARGB = Color.BLUE;
        //notification.ledOnMS =5000;

        return notification;
    }

    /**
     * 显示通知
     * */
    public static void showNotification(Context context, int id, Notification notification){
        // 创建一个NotificationManager的引用
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
    /**
     * 取消通知
     * */
    public static void cancelNotification(Context context, int id){
        // 创建一个NotificationManager的引用
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }



    /**
     * 获得推送的类型，比如订单
     * @param jsonString
     * @return一般返回的是一个数字字符串
     */
    public static String getJsonMsgType(String jsonString) {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        JSONObject msgObj = jsonObject.getJSONObject("msg");
        JSONObject msgBeanObj = msgObj.getJSONObject("msgBean");
        return msgBeanObj.getString("bizType");
    }


    /**
     * 把字符串从Unicode 转换成utf-8的格式
     * @param unicode
     * @return
     */
    public static String fromUnicodeToU8(String unicode) {
        String res = null;
        try {
            res = new String(unicode.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Notification所需要的返回值
     * @param data
     * @return
     */
    public static String getReturnValue(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONObject msgBean = jsonObject.getJSONObject("msgBean");
        JSONObject msgBody = msgBean.getJSONObject("msgBody");
        String param = msgBody.getString("param");
        JSONObject paramBean = JSONObject.parseObject(param);
        JSONObject paramMsgBean = paramBean.getJSONObject("msgBean");
        JSONObject paramMsgBody = paramMsgBean.getJSONObject("msgBody");
        String response = paramMsgBody.getString("content");
        return response;
    }

    public static String getUserName(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONObject msgBean = jsonObject.getJSONObject("msgBean");
        JSONObject msgBody = msgBean.getJSONObject("msgBody");
        return msgBody.getString("pointName");
    }

    /**
     * 返回sessionId
     * @param data
     * @return
     */
    public static Long getSessionIdByJson(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        Long sessionId = jsonObject.getLong("sessionId");
        return sessionId;
    }

    /**
     * 判断应用是否在前台
     * */
    public static boolean isForeground(Context context) {
        ActivityManager am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        if (runningTasks != null){
            // The first in the list of RunningTasks is always the foreground task.
            ActivityManager.RunningTaskInfo foregroundTaskInfo = runningTasks.get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

            if (context.getPackageName().equals(foregroundTaskPackageName)){
                return true;
            }
//            PackageManager pm = context.getPackageManager();
//            PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
//            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
        }

        return false;
    }

    private boolean isRunningForeground(Context context) {
        String packageName = context.getPackageName();
        String topActivityClassName = getTopActivityName(context);
        System.out.println("packageName=" + packageName
                + ",topActivityClassName=" + topActivityClassName);
        if (packageName != null && topActivityClassName != null
                && topActivityClassName.startsWith(packageName)) {
            System.out.println("---> isRunningForeGround");
            return true;
        } else {
            System.out.println("---> isRunningBackGround");
            return false;
        }
    }

    private String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager = (ActivityManager) (context
                .getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager
                .getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();
        }
        return topActivityClassName;
    }
}
