package com.manfenjiayuan.im;

import android.content.Context;
import android.content.Intent;

import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.IMConversationService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.login.logic.MfhLoginService;

import java.text.SimpleDateFormat;

/**
 * Created by bingshanguxue on 14-5-8.
 */
public class IMHelper {
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
//        spSession = SharedPrefesBase.getPreferences(ComnApplication.getAppContext(),
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
        Long guid = msg.getFromGuid();
        Long myId = MfhLoginService.get().getCurrentGuId();
        if(guid == null || myId == null){
            return false;
        }

        //直接比较两个Long值是不相等的，应该是long
        return guid.equals(myId);
    }
}
