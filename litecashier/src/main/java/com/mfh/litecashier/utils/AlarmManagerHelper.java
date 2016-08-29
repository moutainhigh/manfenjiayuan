package com.mfh.litecashier.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.service.DailysettleReceiver;

import java.util.Calendar;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class AlarmManagerHelper {
    public static final String ACTION_DAILYSETTLE = "com.mfh.litecashier.alarm";
    public static final int REQUEST_CODE_DAILYSETTLE = 100;

    /**
     * 注册日结监听器
     * <p/>
     * <p> 1.程序启动时注册<br>
     * 2.检查日结时注册<br>
     * 3.确认日结完成时注册</p>
     * <p/>
     * TODO,在设置中添加取消方法。
     */
    public static void registerDailysettle(Context context) {
        Calendar rightNow = Calendar.getInstance();

        Calendar trigger = Calendar.getInstance();

        if (BizConfig.RELEASE) {
            trigger.add(Calendar.DAY_OF_MONTH, 1);
            trigger.set(Calendar.HOUR_OF_DAY, 0);
            trigger.set(Calendar.MINUTE, 2);
            trigger.set(Calendar.SECOND, 0);
        } else {
            trigger.add(Calendar.MINUTE, 10);
        }
        ZLogger.d(String.format("registerDailysettle : %s", TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(trigger.getTime())));

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

        Intent intent = new Intent(ACTION_DAILYSETTLE);
        intent.setClass(context, DailysettleReceiver.class);

        PendingIntent broadcast = PendingIntent.getBroadcast(context, REQUEST_CODE_DAILYSETTLE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), broadcast);
    }
}
