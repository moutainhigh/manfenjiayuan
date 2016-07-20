package com.mfh.framework.login;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class MfhLoginPreferences {
    //保存上一次用户登录信息
    public static String PREF_NAME_LAST_LOGIN = "login_history";
    public static final String PK_LAST_USERNAME = "last_username";
    public static final String PK_LAST_PASSWORD = "last_password";
    public static final String PK_LAST_OFFICE_ID = "last_office_id";
    public static final String PK_LAST_SPID = "last_spid";

    public static SharedPreferences getLastLoginPreferences(Context context) {
        SharedPreferences pre = context.getSharedPreferences(PREF_NAME_LAST_LOGIN,
                Context.MODE_PRIVATE);//MODE_MULTI_PROCESS
        return pre;
    }

    //保存当前用户登录信息
    public static String PREF_NAME_LOGIN = "login";
    public static final String PK_USERNAME = "username";
    public static final String PK_PASSWORD = "password";
    public static final String PK_LOGIN_USERNAME = "app.login.name";
    public static final String PK_LOGIN_PASSWORD = "app.login.password";
    public static final String PK_LOGIN_SPID = "app.spid";
    public static final String PK_LOGIN_OFFICEID = "app.office.id";
    public static final String PK_LOGIN_USER_GUID = "profile.guid";
    public static final String PK_LOGIN_USER_CPID = "profile.cpid";//channel point id = cpid
    public static final String PK_LOGIN_USER_TELEPHONE = "app.telephone";
    public static final String PK_LOGIN_HTTP_COOKIE = "app.http.cookie";
    public static final String PK_LOGIN_USER_SUBDIS_ID = "app.user.subdisid";
    public static final String PK_LOGIN_USER_ID = "app.user.id";
    public static final String PK_LOGIN_USER_MODULES = "app.user.modules";
    public static final String PK_LOGIN_USER_SUBDIS_NAME = "app.subdisName";
    public static final String PK_LOGIN_SESSION_DATE = "app.session.date";
    public static final String PK_LOGIN_USER_HUMANNAME = "app.user.humanName";
    public static final String PK_LOGIN_USER_HEADIMAGE = "app.headimage";
    public static final String PK_LOGIN_USER_SEX = "app.user.sex";
    public static final String PK_LOGIN_USER_STOCKI_DS = "app.user.stock_ids";
    public static final String PK_LOGIN_USER_CURRENT_STOCKID = "app.user.current.stock.id";
    public static final String PK_LOGIN_USER_CURRENT_OFFICE_NAME = "app.user.current.office.name";
    public static final String PK_LOGIN_USER_OFFICES = "app.user.offices";
    public static final String PK_LAST_SESSION_ID = "last_session_id";

    public static SharedPreferences getLoginPreferences(Context context) {
        if (context == null){
            return null;
        }
        SharedPreferences pre = context.getSharedPreferences(PREF_NAME_LOGIN,
                Context.MODE_PRIVATE);//MODE_MULTI_PROCESS
        return pre;
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
