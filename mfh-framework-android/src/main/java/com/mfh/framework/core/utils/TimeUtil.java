package com.mfh.framework.core.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 工具类 · 时间
 * Created by bingshanguxue on 2015/7/9.
 */
public class TimeUtil {

    public static final String DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a";
    public static final String DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm";

    public static final SimpleDateFormat FORMAT_YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public static final SimpleDateFormat FORMAT_YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final SimpleDateFormat timeDateFormat12 = new SimpleDateFormat(DATE_TIME_FORMAT_12_HOUR, Locale.US);
    public static final SimpleDateFormat timeDateFormat24 = new SimpleDateFormat(DATE_TIME_FORMAT_24_HOUR, Locale.US);

    public static String format(Date date, String template) {
        if (date == null || template == null){
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(template, Locale.US);
        return df.format(date);
    }

    public static String format(Date date, SimpleDateFormat df) {
        if (date == null || df == null){
            return "";
        }
        return df.format(date);
    }


    public static Date parse(String source, SimpleDateFormat df){
        try {
            if (StringUtils.isEmpty(source)){
                return null;
            }
            return df.parse(source);
        } catch (ParseException e) {
//            e.printStackTrace();
            ZLogger.ef(e.toString());
            return null;
        }
    }

    /**
     生成时间戳(10位)
     */
    public static long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 判断是否是同一天
     * */
    public static boolean isSameDay(Date date1, Date date2){
        if (date1 == null && date2 == null){
            return true;
        }

        if (date1 != null && date2 != null){
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(date1);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(date2);

            if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                    && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                    && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)){

//                ZLogger.d("日期是同一天");
                return true;
            }
            else{
//                ZLogger.d("日期不是同一天");
            }
        }

        return false;
    }

}
