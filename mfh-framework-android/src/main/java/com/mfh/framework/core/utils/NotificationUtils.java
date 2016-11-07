package com.mfh.framework.core.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.mfh.framework.R;


/**
 * Notification
 * Created by bingshanguxue on 28/10/2016.
 */

public class NotificationUtils {
    public static String ACTION_CLICK = "com.manfenjiayuan.intent.action.NotificationClick";


    /**
     * @param context
     * @param icon small icon
     * @param title title
     * @param text detail text
     * */
    public static void makeNotification(Context context, int icon, String title, String text,
                                        Class<?> cls){
        //Create Notification Builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        // Setting Notification Properties
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setAutoCancel(true);//点击后自动取消


        // Attach Actions
        Intent resultIntent = new Intent(context, cls);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(cls);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Issue the notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    public static void showMessageInNotificationBar(Context context, String title, String content) {

//Create Notification Builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        // Setting Notification Properties
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setAutoCancel(true);

        RemoteViews remoteViews;
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notification);
        remoteViews.setTextViewText(R.id.tv_title, title);
        remoteViews.setTextViewText(R.id.tv_content, content);
        mBuilder.setContent(remoteViews);

        Notification notification = mBuilder.build();
        //设定Notification出现时的声音
        notification.defaults |= Notification.DEFAULT_SOUND;
        //设定如何振动
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE
        );
        Intent notificationIntent = new Intent(ACTION_CLICK);
        notificationIntent.putExtra("content", content);
        PendingIntent broadcast = PendingIntent.getBroadcast(
                context, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );

        remoteViews.setOnClickPendingIntent(R.id.tv_content, broadcast);

        int mNotificationId = 52494791;
        notificationManager.notify(mNotificationId, notification);
    }
}
