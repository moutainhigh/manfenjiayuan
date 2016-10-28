package com.manfenjiayuan.pda_supermarket.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

/**
 * Notification
 * Created by bingshanguxue on 28/10/2016.
 */

public class NotificationUtils {

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


        // Attach Actions
        Intent resultIntent = new Intent(context, cls);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(cls);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);


        // Issue the notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }
}
