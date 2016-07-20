package com.mfh.framework.core.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.mfh.framework.core.logger.ZLogger;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 接收推送数据的工具类
 * Created by 李潇阳 on 2014/9/4.
 */
public class PushUtil {

    /**
     * generate new notificatioin
     * */
    public static Notification generateNotification(Context context, String title, String text, int iconResId){
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(iconResId)
                .setAutoCancel(true)
                .setWhen(TimeUtil.genTimeStamp())
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
     * 通过getRunningTasks判断App是否位于前台，此方法在5.0以上失效
     * @param context     上下文参数
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
    /**
     * 方法4：通过使用UsageStatsManager获取，此方法是ndroid5.0A之后提供的API
     * 必须：
     * 1. 此方法只在android5.0以上有效
     * 2. AndroidManifest中加入此权限<uses-permission xmlns:tools="http://schemas.android.com/tools" android:name="android.permission.PACKAGE_USAGE_STATS"
     * tools:ignore="ProtectedPermissions" />
     * 3. 打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean queryUsageStats(Context context, String packageName) {
        class RecentUseComparator implements Comparator<UsageStats> {
            @Override
            public int compare(UsageStats lhs, UsageStats rhs) {
                return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
            }
        }
        RecentUseComparator mRecentComp = new RecentUseComparator();
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000 * 10, ts);
        if (usageStats == null || usageStats.size() == 0) {
            if (!HavaPermissionForTest(context)) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Toast.makeText(context, "权限不够\n请打开手机设置，点击安全-高级，在有权查看使用情况的应用中，为这个App打上勾", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        Collections.sort(usageStats, mRecentComp);
        String currentTopPackage = usageStats.get(0).getPackageName();
        if (currentTopPackage.equals(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否有用权限
     *
     * @param context 上下文参数
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean HavaPermissionForTest(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    private boolean isRunningForeground(Context context) {
        String packageName = context.getPackageName();
        String topActivityClassName = getTopActivityName(context);
        ZLogger.d("packageName=" + packageName
                + ",topActivityClassName=" + topActivityClassName);
        if (packageName != null && topActivityClassName != null
                && topActivityClassName.startsWith(packageName)) {
            ZLogger.d("---> isRunningForeGround");
            return true;
        } else {
            ZLogger.d("---> isRunningBackGround");
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
