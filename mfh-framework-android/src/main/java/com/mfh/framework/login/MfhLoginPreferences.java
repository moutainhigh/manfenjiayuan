package com.mfh.framework.login;

import android.content.Context;
import android.content.SharedPreferences;

import com.mfh.framework.prefs.SharedPrefesUltimate;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class MfhLoginPreferences extends SharedPrefesUltimate {
    //保存上一次用户登录信息
    public static String PREF_NAME_LAST_LOGIN = "login_history";
    public static final String PK_LAST_USERNAME = "last_username";
    public static final String PK_LAST_PASSWORD = "last_password";
    public static final String PK_LAST_OFFICE_ID = "last_office_id";
    public static final String PK_LAST_SPID = "last_spid";

    public static SharedPreferences getLastLoginPreferences(Context context) {
        return getSharedPreferences(context, PREF_NAME_LAST_LOGIN);
    }

    //保存当前用户登录信息
    public static String PREF_NAME_LOGIN = "pref_login";
    public static final String PK_USERNAME = "PK_USERNAME";//登录用户名
    public static final String PK_PASSWORD = "PK_PASSWORD";//登录密码
    public static final String PK_SPID = "PK_SPID";
    public static final String PK_GUID = "PK_GUID";
    public static final String PK_HUMAN_ID = "PK_HUMAN_ID";
    public static final String PK_HUMAN_NAME = "PK_HUMAN_NAME";//用户昵称
    public static final String PK_OWNER_ID = "PK_OWNER_ID";
    public static final String PK_CPID = "PK_CPID";//channel point id = cpid
    public static final String PK_TELEPHONE = "PK_TELEPHONE";
    public static final String PK_COOKIE = "PK_COOKIE";
    public static final String PK_SUBDIS_IDS = "PK_SUBDIS_IDS";
    public static final String PK_SUBDIS_NAMES = "PK_SUBDIS_NAMES";
    public static final String PK_USER_ID = "PK_USER_ID";
    public static final String PK_MODULE_NAMES = "PK_MODULE_NAMES";
    public static final String PK_SESSION_DATE = "PK_SESSION_DATE";
    public static final String PK_HEAD_IMAGE = "PK_HEAD_IMAGE";
    public static final String PK_SEX = "PK_SEX";
    public static final String PK_STOCK_IDS = "PK_STOCK_IDS";
    public static final String PK_STOCK_CURRENT_ID = "PK_STOCK_CURRENT_ID";
    public static final String PK_OFFICE_CURRENT_ID = "PK_OFFICE_CURRENT_ID";
    public static final String PK_OFFICE_CURRENT_NAME = "PK_OFFICE_CURRENT_NAME";
    public static final String PK_OFFICES = "PK_OFFICES";
    public static final String PK_SESSION_ID = "PK_SESSION_ID";

    public static SharedPreferences getLoginPreferences(Context context) {
        return getSharedPreferences(context, PREF_NAME_LOGIN);
    }

    /**
     * 清空当前登录信息
     */
    public static void clearLoginPreferences(Context context){
        SharedPreferences sp = getLoginPreferences(context);
        if (sp != null){
            SharedPreferences.Editor editor = sp.edit();
            editor.clear();
            editor.commit();
        }
    }
}
