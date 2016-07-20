package com.manfenjiayuan.im;

import android.content.Context;
import android.content.Intent;

import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.IMConversationService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 14-5-8.
 */
public class IMHelper {
    /**
     * 通过传进来的时间返回翻译时间，如：早上 6：00
     * @param time
     * @param type 类型，昨天，前天是否带后续时间(in,代表是对话里面，带时间，out不带)
     * @return
     */
    public static String getCaptionTime (String time, String type) {
        Calendar calendar = Calendar.getInstance();
        //获取系统当前时间
        //年
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));
        String year = time.substring(0, 4);
        //月
        String currentMon = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        if (currentMon.length() == 1) {
            currentMon = "0" + currentMon;
        }
        String mon = time.substring(5, 7);
        //日
        Integer currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        String day = time.substring(8, 10);
        if (day.substring(0, 1).equals(0) && day.length() > 1)
            day.substring(1);

        Integer inDay = 0;
        if (day != null && StringUtils.isDigit(day))
            inDay = Integer.valueOf(day);

        //判断年月是否相等
        if (currentMon.equals(mon) && currentYear.equals(year)) {
            //判断日是否相等
            if (currentDay == inDay) {
                return getCaptionDay(time);
            }
            else if (currentDay - 1 == inDay) {
                if ("in".equals(type))//里面的时间
                    return "昨天 " + getCaptionDay(time);
                else//外面的时间
                    return "昨天";
            }
            else if (currentDay - 2 == inDay) {
                if ("in".equals(type))//里面的时间
                    return "前天 " + getCaptionDay(time);
                else//外面的时间
                    return "前天";
            }

        }
        else {
            return time.substring(5, 16);
        }

        return time.substring(5, 16);
    }

    public static String  getCaptionDay (String time) {
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
        else if (theHour >18 && theHour <= 23)
            return "晚上" + trimTime.substring(11, 16);
        else
            return time.substring(5, 16);
    }

    /**
     * 第一次登陆，把对话等都油标都设置为最大
     */
    public void setLastUpdateDate() {
        IMConversationService sessionService = ServiceFactory.getService(IMConversationService.class);
        sessionService.queryFromNetToSaveMaxUpDateDate();
    }

    public static final SimpleDateFormat SDF_INNER_DATAFORMAT = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);
//
//    private static String SP_NAME_SUFFIX = "emb.session";
//    private static final String PREF_KEY_LAST_UPDATE = "lastUpdate";//最后一次更新时间
//    private static final String PREF_KEY_MAX_UPDATE = "maxUpdate";//登录时间
//

//    private static SharedPreferences spSession = null;
//    public static void restore(){
//        spSession = SharedPreferencesUtil.getPreferences(ComnApplication.getAppContext(),
//                SharedPreferencesHelper.getLoginUsername() + "." + SP_NAME_SUFFIX);
//    }
//    public static void clear(){
//        spSession = null;
//    }

    /**
     * 改变一个会话的未读消息数
     * @param sessionId
     * @param totalCount 若是正数则增加，负数则减少。
     */
    public static void changeSessionUnReadCount(Context context, Long sessionId, int totalCount) {
        IMConversationService ss = ServiceFactory.getService(IMConversationService.class);
        if(ss == null){
            return;
        }

        //update
        ss.getDao().addUnReadCount(sessionId, totalCount);

        //刷新当前会话的未读消息个数
        Intent intentSession = new Intent(IMConstants.ACTION_REFRESH_SESSIONUNREAD);
        intentSession.putExtra(IMConstants.EXTRA_NAME_SESSION_ID, sessionId);
        context.sendBroadcast(intentSession);

        //刷新当前用户所有消息的未读个数
        Integer unReadCount = ss.getDao().getTotalUnReadCount(MfhLoginService.get().getLoginName());
        if (unReadCount != null && unReadCount != -1) {
            sendBroadcastForUpdateUnread(context, unReadCount);
        }
    }

    /**
     * 广播：刷新未读消息数目
     * */
    public static void sendBroadcastForUpdateUnread(Context context, int unreadCount){
        Intent intent = new Intent(IMConstants.ACTION_REFRESH_ALLUNREAD);
        intent.putExtra(IMConstants.PARAM_unReadCount, unreadCount);
        intent.putExtra(IMConstants.PARAM_tabIndex, 0);//第一个tab
        context.sendBroadcast(intent);
    }

    /**
     * 判断是否自己的消息
     * @param msg
     * @return
     */
    public static boolean isMySelf(EmbMsg msg) {
        Long guid = msg.getFromguid();
        Long myId = MfhLoginService.get().getCurrentGuId();
        if(guid == null || myId == null){
            return false;
        }

        //直接比较两个Long值是不相等的，应该是long
        return guid.equals(myId);
    }
}
