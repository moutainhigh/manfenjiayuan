package com.mfh.framework.core.utils;

/**
 * 按钮重复点击
 *
 * */
public class BtnClickUtils {
    private static long mLastClickTime = 0;

    private BtnClickUtils( ){

    }

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        if ( 0 < timeD && timeD < 1000) {
            return true;
        }

        mLastClickTime = time;

        return false;
    }
}
