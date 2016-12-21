package com.mfh.litecashier.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.ZIntent;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.litecashier.CashierApp;

import java.util.Calendar;

/**
 * 定时器
 * Created by bingshanguxue on 16/2/24.
 */
public class AlarmManagerHelper {
    public static final int REQUEST_CODE_DAILYSETTLE = 100;
    public static final int REQUEST_CODE_BUGLY_UPGRADE = 101;

    /**
     * 触发下一次日结
     * @param type <ul>
     *             <li>0,失败或异常情况，正常解锁后，6小时后自动重试 </li>
     *             <li>1,锁定后，1小时后自动重试 </li>
     *             <li>others,10分钟小时后自动重试 </li>
     *             </ul>
     *
     * */
    public static void triggleNextDailysettle(int type){
        Calendar trigger = Calendar.getInstance();
        //锁定后，10分钟后自动重试，多台POS可以自动解锁
        if (type == 0){
            trigger.add(Calendar.HOUR_OF_DAY, 6);
        }
        if (type == 1){
            trigger.add(Calendar.HOUR_OF_DAY, 1);
        }
        //失败或异常情况，正常解锁后，6小时后自动重试
        else{
            trigger.add(Calendar.MINUTE, 10);
        }

        registerDailysettle(CashierApp.getAppContext(), trigger);
    }

    /**
     * 注册日结监听器
     * <p/>
     * <p> 1.程序启动时注册<br>
     * 2.检查日结时注册<br>
     * 3.确认日结完成时注册</p>
     * <p/>
     * TODO,在设置中添加取消方法。
     */
    public static void registerDailysettle(Context context, Calendar trigger) {
        Calendar rightNow = Calendar.getInstance();
        ZLogger.d(String.format("trigger : %s",
                TimeUtil.format(trigger.getTime(), TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

//        java.util.GregorianCalendar[time=?,areFieldsSet=false,lenient=true,zone=GMT,firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,
//                YEAR=2016,
//                MONTH=1,
//                WEEK_OF_YEAR=9,WEEK_OF_MONTH=4,
//                DAY_OF_MONTH=24,DAY_OF_YEAR=55,DAY_OF_WEEK=4,DAY_OF_WEEK_IN_MONTH=4,
//                AM_PM=0,
//                HOUR=11,HOUR_OF_DAY=11,
//                MINUTE=14,
//                SECOND=0,
//                MILLISECOND=991,
//                ZONE_OFFSET=0,DST_OFFSET=0]
//        ZLogger.d(String.format("register: %s", trigger.toString()));
        if (rightNow.after(trigger)) {
            return;
        }

        Intent intent = new Intent(ZIntent.ACTION_DAILYSETTLE);
        intent.setClass(context, DailysettleReceiver.class);
//        intent.putExtra()

        PendingIntent broadcast = PendingIntent.getBroadcast(context, REQUEST_CODE_DAILYSETTLE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), broadcast);//定时，不重复
    }

    /**
     * 注册Bugly自动检测更新
     * <p/>
     * <p> 1.程序启动后每隔12小时检测一次<br>
     * <p/>
     * TODO,在设置中添加取消方法。
     */
    public static void registerBuglyUpgrade(Context context) {
        // Set the alarm to start at 1:00 a.m.
        Calendar trigger = Calendar.getInstance();
//            trigger.add(Calendar.DAY_OF_MONTH, 1);
        trigger.add(Calendar.HOUR_OF_DAY, 1);
        trigger.set(Calendar.MINUTE, 0);
        trigger.set(Calendar.SECOND, 0);

        Calendar rightNow = Calendar.getInstance();
        ZLogger.d(String.format("trigger : %s",
                TimeUtil.format(trigger.getTime(), TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

        if (rightNow.after(trigger)) {
            return;
        }

        Intent intent = new Intent(ZIntent.ACTION_BETA_BUGLY_CHECKUPDATE);
        intent.setClass(context, KeepAlarmLiveReceiver.class);

//        PendingIntent broadcast = PendingIntent.getBroadcast(context, REQUEST_CODE_BUGLY_UPGRADE, intent,
//                FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BUGLY_UPGRADE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // setRepeating() lets you specify a precise custom interval--in this case, 20 minutes.
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(),
                AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
    }
}
