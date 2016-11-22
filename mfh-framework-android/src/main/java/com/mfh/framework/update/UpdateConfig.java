package com.mfh.framework.update;

import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.network.NetFactory;

/**
 * 应用更新参数配置
 * Created by bingshanguxue on 5/18/16.
 */
public class UpdateConfig {

    public static final String PREF_APPUPDATE = "pref_appupdate";
    public static final String PK_B_UPDATE_AUTOPOPUP = "pk_appupdate_updateautopopup"; //主机IP
    public static final String PK_I_SMSCALE_PORT = "pk_smscale_port";   //主机端口号
    public static final String PK_S_SMSCALE_USERNAME = "pk_smscale_username";//用户名
    public static final String PK_S_SMSCALE_PASSWORD = "pk_smscale_password";//密码
    public static final String PK_S_SMSCALE_ENCODING = "pk_smscale_encoding";//文件编码
    public static final String PK_B_SMSCALE_FULLSCALE = "pk_smscale_fullscale";//全量更新
    public static final String PK_S_SMSCALE_LASTCURSOR = "pk_smscale_lastcursor";//更新游标


    public static String URL_APP_UPDATE = NetFactory.getUpdateServerUrl();

    /**
     * 获取服务器上最新版本
     * */
    public static final String URL_APP_UPDATE_VERSIOIN = URL_APP_UPDATE + "/app/update/version";
    /**
     * 下载Apk文件
     * */
    public static final String URL_APP_UPDATE_DOWNLOAD = URL_APP_UPDATE + "/app/update/download";


    private boolean isUpdateAutoPopup;


    public static boolean isUpdateAutoPopup() {
        return SharedPrefesManagerFactory.getBoolean(PREF_APPUPDATE, PK_B_UPDATE_AUTOPOPUP, false);
    }

    public void setUpdateAutoPopup(boolean updateAutoPopup) {
        isUpdateAutoPopup = updateAutoPopup;
    }
}
