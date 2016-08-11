package com.bingshanguxue.pda.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.BizConfig;
import com.mfh.framework.ZIntent;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;

import java.util.Calendar;

/**
 * 定时器
 * Created by bingshanguxue on 16/2/24.
 */
public class AlarmManagerHelper {
    public static final int REQUEST_CODE_DAILYSETTLE = 100;
    public static final int REQUEST_CODE_BUGLY_UPGRADE = 101;


    /**
     * 注册Bugly自动检测更新
     * <p/>
     * <p> 1.程序启动后每隔一小时检测一次<br>
     * <p/>
     * TODO,在设置中添加取消方法。
     */
    public static void registerBuglyUpgrade(Context context) {
        Calendar trigger = Calendar.getInstance();

        if (BizConfig.RELEASE) {
            //第二天凌晨2点钟
//            trigger.add(Calendar.DAY_OF_MONTH, 1);
//            trigger.add(Calendar.HOU/R_OF_DAY, 1);
            trigger.add(Calendar.MINUTE, 5);
//            trigger.add(Calendar.SECOND, 10);
        } else {
            trigger.add(Calendar.SECOND, 10);
        }

        Calendar rightNow = Calendar.getInstance();
        ZLogger.d(String.format("trigger : %s",
                TimeUtil.format(trigger.getTime(), TimeCursor.FORMAT_YYYYMMDDHHMMSS)));

        if (rightNow.after(trigger)) {
            return;
        }

        Intent intent = new Intent(ZIntent.ACTION_BETA_BUGLY_CHECKUPDATE);
        intent.setClass(context, KeepAlarmLiveReceiver.class);

        PendingIntent broadcast = PendingIntent.getBroadcast(context, REQUEST_CODE_BUGLY_UPGRADE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), broadcast);
    }
}
