package com.manfenjiayuan.im;

import android.content.SharedPreferences;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesBase;
import com.mfh.framework.prefs.SharedPrefesUltimate;

/**
 * Created by bingshanguxue on 16/3/2.
 */
public class IMConfig {
    /**
     * 更新用户身份ID*/
    public static void updateIdentify(Long humanId){
        SP_NAME_SSESSION = "emb.session" + humanId;
    }

    public static final String PREF_NAME_MSG_BRIDGE = "pref_msg_bridge";
    public static final String PREF_KEY_MSGBRIDGE_CLIENTID = "clientid";

    public static String getPushClientId(){
        return SharedPrefesBase.getString(MfhApplication.getAppContext(),
                PREF_NAME_MSG_BRIDGE, PREF_KEY_MSGBRIDGE_CLIENTID, null);
    }

    public static void savePushClientId(String clientId){
        SharedPrefesBase.set(MfhApplication.getAppContext(),
                PREF_NAME_MSG_BRIDGE, PREF_KEY_MSGBRIDGE_CLIENTID, clientId);
    }


    private static String SP_NAME_SSESSION = "emb.session";
    private static final String PREF_KEY_LAST_UPDATE = "lastUpdate";//最后一次更新时间
    private static final String PREF_KEY_MAX_UPDATE = "maxUpdate";//登录时间


    /**
     * 清理所有配置
     */
    public static void clearSessionConfig() {
        SharedPrefesUltimate.clear(SP_NAME_SSESSION);
    }

    /**
     * 获取最大下载的会话数
     * @return
     */
    public static int getMaxSessionNum() {
        return SharedPrefesUltimate.getInt(SP_NAME_SSESSION, "maxSessionNum", 200);
    }

    /**
     * 设置最大会话数
     * @param sessionNum
     */
    public static void setMaxSessionNum(int sessionNum) {
        SharedPrefesUltimate.set(SP_NAME_SSESSION, "maxSessionNum", sessionNum);
    }

    /**
     * 获取最大下载的消息数/单会话
     * @return
     */
    public static int getMaxMsgNumOneSession() {
        return SharedPrefesUltimate.getInt(SP_NAME_SSESSION, "maxMsgNumOneSession", 1000);
    }

    /**
     * 设置最大下载的消息数/单会话
     * @param sessionNum
     */
    public static void setMaxMsgNumOneSession(int sessionNum) {
        SharedPrefesUltimate.set(SP_NAME_SSESSION, "maxMsgNumOneSession", sessionNum);
    }

    /**
     * 获取session会话的游标，用于向服务器端增量请求数据
     * @return
     */
    public static Long getLastUpdate() {
         return SharedPrefesUltimate.getLong(SP_NAME_SSESSION, PREF_KEY_LAST_UPDATE, -1L);
    }

    /**
     * 清理会话游标
     */
    public void clearLastUpdate() {
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.remove(PREF_KEY_LAST_UPDATE);
        editor.commit();
    }

    /**
     * 保存会话游标
     * @param updateTime
     */
    public static void saveLastUpdate(Long updateTime) {
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.putLong(PREF_KEY_LAST_UPDATE, updateTime);
        editor.commit();
    }

    /**
     * 获取session会话的游标，用于向服务器端增量请求数据
     * @return
     */
    public Long getMaxId(Long sessionId) {
        return SharedPrefesUltimate.getLong(SP_NAME_SSESSION, "maxMsgId_" + sessionId.toString(), -1L);
    }

    /**
     * 保存会话游标
     * @param maxMsgId
     */
    public static void saveMaxId(Long sessionId, Long maxMsgId) {
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.putLong("maxMsgId_" + sessionId.toString(), maxMsgId);
        editor.commit();
    }

    /**
     * 获得会话游标
     * @param sessionId
     * @return
     */
    public static String getMaxCreateTime(Long sessionId) {
        String maxCreateTime = SharedPrefesUltimate.getString(SP_NAME_SSESSION,
                "maxCreateTime_" + sessionId, "0000-00-00 00:00:00");
        if (StringUtils.isEmpty(maxCreateTime))
            return "0000-00-00 00:00:00";
        else
            return maxCreateTime;
    }

    /**
     * 保存消息游标
     * @param sessionId
     * @param createTiem
     */
    public static void saveMaxCreateTime(Long sessionId, String createTiem) {
        ZLogger.d(String.format("sessionId=%s, createTime=%s", String.valueOf(sessionId), createTiem));
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.putString("maxCreateTime_" + sessionId, createTiem);
        editor.commit();
    }

    /**
     * 保存session的Msg最大游标
     * @param sessionId
     * @param lastUpdate
     */
    public void saveMaxMsgLastUpdateDate(Long sessionId, String lastUpdate) {
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.putString("maxMsgLastUpdateDate_" + sessionId, lastUpdate);
        editor.commit();
    }

    /**
     * 获取session的Msg最大游标
     * @param sessionId
     * @return
     */
    public static String getMaxMsgLastUpdateDate(Long sessionId) {
        return SharedPrefesUltimate.getString(SP_NAME_SSESSION, "maxMsgLastUpdateDate_" + sessionId, "");
    }

    public static void setMaxMsgUpdateDate(String updateDate) {
        SharedPreferences.Editor editor = SharedPrefesUltimate.getDefaultEditor(SP_NAME_SSESSION);
        editor.putString(PREF_KEY_MAX_UPDATE, updateDate);
        editor.commit();
    }

    public static String getMaxMsgUpdateDate() {
        return SharedPrefesUltimate.getString(SP_NAME_SSESSION, PREF_KEY_MAX_UPDATE, "");
    }

}
