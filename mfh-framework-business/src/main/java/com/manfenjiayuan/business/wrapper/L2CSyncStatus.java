package com.manfenjiayuan.business.wrapper;

/**
 * Created by bingshanguxue on 4/29/16.
 */
public class L2CSyncStatus {
    public static final Integer SYNC_STATUS_INIT            = 0;//未同步（可以同步）
    public static final Integer SYNC_STATUS_SYSTEM_ERROR    = 1;//系统异常（可以同步）
    public static final Integer SYNC_STATUS_FINISHED        = 4;//已同步
    public static final Integer SYNC_STATUS_PARAMS_ERROR    = 5;//参数异常（不可以同步）
    public static final Integer SYNC_STATUS_ERROR           = 7;//错误数据（不可以同步）

    public static String translate(Integer value) {
        if (value.equals(SYNC_STATUS_INIT)) {
            return "未同步";
        }
        else if (value.equals(SYNC_STATUS_SYSTEM_ERROR)) {
            return "系统异常";
        }
        else if (value.equals(SYNC_STATUS_FINISHED)) {
            return "已同步";
        }
        else if (value.equals(SYNC_STATUS_PARAMS_ERROR)) {
            return "参数异常";
        }
        else if (value.equals(SYNC_STATUS_ERROR)) {
            return "错误数据";
        }
        else{
            return "未知";
        }
    }
}
