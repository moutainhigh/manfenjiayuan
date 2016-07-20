package com.mfh.owner.utils;

import android.content.Context;

import com.mfh.comn.config.UConfig;
import com.mfh.comn.upgrade.DbVersion;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.database.dao.BaseDbDao;

/**
 * Created by bingshanguxue on 2015/2/14.
 */
public class Constants {
    public static final int ACTIVITY_REQUEST_CODE_ACCOUNT_LOGIN = 0X00;
    public static final int ACTIVITY_REQUEST_CODE_ZXING_QRCODE  = 0X01;
    public static final int ACTIVITY_REQUEST_CODE_SETTINGS      = 0X02;
    public static final int ACTIVITY_REQUEST_SEARCH_RESULT      = 0X03;
    public static final int ACTIVITY_REQUEST_SELECT_POSITION    = 0X04;
    public static final int ACTIVITY_REQUEST_PAY                = 0X05;//订单充值
    public static final int ACTIVITY_REQUEST_ME_ORDER           = 0X06;//订单充值
    public static final int ACTIVITY_REQUEST_ME_PACKAGE         = 0X07;//卡包
    public static final int ACTIVITY_REQUEST_ME_CART            = 0X08;//购物车
    public static final int ACTIVITY_REQUEST_RECEIVE_STOCK      = 0X09;//默认收货网点
    public static final int ACTIVITY_REQUEST_SUBDIS_SELECT      = 0X0A;//常住小区
    public static final int ACTIVITY_REQUEST_CHANGE_NICKNAME    = 0X0B;//修改昵称
    public static final int ACTIVITY_REQUEST_CHANGE_LOGINPWD    = 0X0C;//修改登录密码
    public static final int ACTIVITY_REQUEST_LOGIN_H5           = 0X0D;//修改登录密码
    public static final int ACTIVITY_REQUEST_CHANGE_ORDER       = 0X0E;//计划


    public static final String INTENT_KEY_IS_LOGOUT = "INTENT_KEY_IS_LOGOUT";


    public static final String BROADCAST_ACTION_CHANGE_BACKGROUND  = "BROADCAST_ACTION_CHANGE_BACKGROUND";
    public static final String BROADCAST_KEY_BACKGROUND_MASK_VISIBILITY = "BROADCAST_KEY_BACKGROUND_MASK_VISIBILITY";
    public static final String BROADCAST_ACTION_TOGGLE_MAIN_TABHOST  = "BROADCAST_ACTION_TOGGLE_MAIN_TABHOST";
    public static final String BROADCAST_KEY_MAIN_TABHOST_VISIBILITY = "BROADCAST_KEY_MAIN_TABHOST_VISIBILITY";


    public static final String ACTION_BEACONS_UPDATE = "ACTION_BEACONS_UPDATE";
    public static final String KEY_BEACONS_EXIST = "KEY_BEACONS_EXIST";

    public static final String ACTION_WXPAY_PAYID           = "ACTION_WXPAY_PAYID";//
    public static final String BROADCAST_KEY_PREPAY_ID           = "prepay_id";//
}
