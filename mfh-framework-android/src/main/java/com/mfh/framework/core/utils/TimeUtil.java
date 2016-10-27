package com.mfh.framework.core.utils;

import com.mfh.framework.anlaysis.logger.ZLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.media.CamcorderProfile.get;

/**
 * the utility of time
 * Created by bingshanguxue on 2015/7/9.
 */
public class TimeUtil {

    public static final String DATE_TIME_FORMAT_12_HOUR = "MMM d, yyyy  h:mm a";
    public static final String DATE_TIME_FORMAT_24_HOUR = "MMM d, yyyy  k:mm";

    public static final SimpleDateFormat FORMAT_YYYYMMDDHHMM = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public static final SimpleDateFormat FORMAT_YYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    public static final SimpleDateFormat FORMAT_MMDD = new SimpleDateFormat("MM-dd ", Locale.US);
    public static final SimpleDateFormat FORMAT_HHMM = new SimpleDateFormat("HH:mm", Locale.US);
    public static final SimpleDateFormat timeDateFormat12 = new SimpleDateFormat(DATE_TIME_FORMAT_12_HOUR, Locale.US);
    public static final SimpleDateFormat timeDateFormat24 = new SimpleDateFormat(DATE_TIME_FORMAT_24_HOUR, Locale.US);

    public static String format(Date date, String template) {
        if (date == null || template == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(template, Locale.US);
        return df.format(date);
    }

    public static String format(Date date, SimpleDateFormat df) {
        if (date == null || df == null) {
            return "";
        }
        return df.format(date);
    }


    public static Date parse(String source, SimpleDateFormat df) {
        try {
            if (StringUtils.isEmpty(source)) {
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
     * 生成时间戳(10位)
     */
    public static long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 判断是否是同一天:年月日相同
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return true;
        }

        if (date1 != null && date2 != null) {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(date1);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(date2);

            if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                    && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
                    && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)) {

//                ZLogger.d("日期是同一天");
                return true;
            } else {
//                ZLogger.d("日期不是同一天");
            }
        }

        return false;
    }

    /**
     * 将一个时间戳转换成提示性时间字符串，如刚刚，1秒前
     *
     * @param timeStamp
     * @return
     */
    public static String convertTimeToFormat(long timeStamp) {
        long curTime = System.currentTimeMillis() / (long) 1000;
        long time = curTime - timeStamp;

        if (time < 60 && time >= 0) {
            return "刚刚";
        } else if (time >= 60 && time < 3600) {
            return time / 60 + "分钟前";
        } else if (time >= 3600 && time < 3600 * 24) {
            return time / 3600 + "小时前";
        } else if (time >= 3600 * 24 && time < 3600 * 24 * 30) {
            return time / 3600 / 24 + "天前";
        } else if (time >= 3600 * 24 * 30 && time < 3600 * 24 * 30 * 12) {
            return time / 3600 / 24 / 30 + "个月前";
        } else if (time >= 3600 * 24 * 30 * 12) {
            return time / 3600 / 24 / 30 / 12 + "年前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 通过传进来的时间返回翻译时间，如：早上 6：00
     *
     * @param date yyyy-MM-dd HH:mm:ss
     * @param type 类型，昨天，前天是否带后续时间(in,代表是对话里面，带时间，out不带)
     * @return
     */
    public static String getCaptionTime(Date date) {
        if (date == null) {
            return null;
        }

        //获取系统当前时间
        Calendar calendar = Calendar.getInstance();
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        String currentMon = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        if (currentMon.length() == 1) {
            currentMon = "0" + currentMon;
        }
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        String formatTime = format(date, FORMAT_YYYYMMDDHHMMSS);
        String year = formatTime.substring(0, 4);//年
        String mon = formatTime.substring(5, 7);//月
        String day = formatTime.substring(8, 10);//日
        String hour = formatTime.substring(11, 13);//时

        if (day.length() > 1 && day.substring(0, 1).equals("0")) {
            day.substring(1);
        }

        Integer inDay = 0;
        if (StringUtils.isDigit(day)) {
            inDay = Integer.valueOf(day);
        }

        //判断年月是否相等
        if (currentYear.equals(year) && currentMon.equals(mon)) {
            //判断日是否相等
            if (currentDay == inDay) {
                return "今天 " + getCaptionDay(formatTime);
            } else if (currentDay + 1 == inDay) {
                return "明天" + getCaptionDay(formatTime);
            } else if (currentDay - 1 == inDay) {
                return "昨天 " + getCaptionDay(formatTime);
            } else if (currentDay - 2 == inDay) {
                return "前天 " + getCaptionDay(formatTime);
            }
        }

        //MM-dd HH:mm
        return formatTime.substring(5, 16);
    }

    public static String getCaptionTimeV2(Date date, boolean timeCaptionEnabled) {
        if (date == null) {
            return null;
        }

        //获取系统当前时间
        Calendar rightNow = Calendar.getInstance();
        Calendar check = Calendar.getInstance();
        check.setTime(date);

        String dayCaption = format(date, FORMAT_YYYYMMDD);
        String timeCaption = format(date, FORMAT_HHMM);

        //判断年月是否相等
        if (check.get(Calendar.YEAR) == rightNow.get(Calendar.YEAR)
                && check.get(Calendar.MONTH) == rightNow.get(Calendar.MONTH)) {
            //判断日是否相等
            int today = rightNow.get(Calendar.DAY_OF_MONTH);
            int day = check.get(Calendar.DAY_OF_MONTH);
//            ZLogger.d("today=" + today);
//            ZLogger.d("day=" + day);

            if (day == today) {
                dayCaption = "今天";
            } else if (day - 1 == today) {
                dayCaption = "明天";
            } else if (day + 1 == today) {
                dayCaption = "昨天";
            } else if (day + 2 == today) {
                dayCaption = "前天";
            }
        }

        if (timeCaptionEnabled){
            int hour = check.get(Calendar.HOUR_OF_DAY);
//            ZLogger.d("hour=" + hour);
            if (hour >= 0 && hour <= 5){
                timeCaption = "凌晨";
            }
            else if (hour > 5 && hour <= 12){
                timeCaption = "早上";
            }
            else if (hour > 12 && hour <= 18){
                timeCaption = "下午";
            }
            else if (hour > 18 && hour <= 23){
                timeCaption = "晚上";
            }
        }

        //MM-dd HH:mm
        return String.format("%s %s", dayCaption, timeCaption);
    }

    public static String getCaptionDay(String time) {
        String trimTime = time.trim();
        //获得时间
        String hour = trimTime.substring(11, 13);
        Integer theHour = Integer.valueOf(hour);
        if (theHour >= 0 && theHour <= 5)
            return "凌晨" + trimTime.substring(11, 16);
        else if (theHour > 5 && theHour <= 11)
            return "早上" + trimTime.substring(11, 16);
        else if (theHour > 11 && theHour <= 17)
            return "下午" + trimTime.substring(11, 16);
        else if (theHour > 18 && theHour <= 23)
            return "晚上" + trimTime.substring(11, 16);
        else
            return time.substring(5, 16);
    }

    /**
     * 生成时间段
     * @param curHour 当前小时
     * @param curMin 当前分钟
     * @param maxHour 最大小时
     * */
    private String[] genTimeSpan(int minHour, int curMin, int maxHour){
        ZLogger.d(String.format("%d:%d", minHour, curMin));
        List<String> timeList = new ArrayList<>();
        if (curMin == 0){
            //临界点，从当前小时开始配送
            for (int h = minHour; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }
        else if (curMin > 0 && curMin < 30){//未超过30分从当前小时的30分开始
            for (int h = minHour; h < maxHour; h++){
                String timeStr = String.format("%2d:30-%2d:30", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }
        else{//超过30分从下一个小时开始
            for (int h = minHour+1; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }

        return timeList.toArray(new String[timeList.size()]);
    }

    /**
     * 生成时间段
     * @param curHour 当前小时
     * @param curMin 当前分钟
     * @param maxHour 最大小时
     * */
    public static String[] genTimeSpanV2(int minHour, int curMin, int maxHour){
        ZLogger.d(String.format("genTimeSpanV2: %d:%d", minHour, curMin));
        List<String> timeList = new ArrayList<>();
        if (curMin == 0){
            //临界点，从当前小时开始配送
            for (int h = minHour; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }
        else{//从下一个小时开始
            for (int h = minHour+1; h <= maxHour; h++){
                String timeStr = String.format("%2d:00-%2d:00", h, h+1);

                ZLogger.d(timeStr);
                timeList.add(timeStr);
            }
        }

        return timeList.toArray(new String[timeList.size()]);
    }

}
