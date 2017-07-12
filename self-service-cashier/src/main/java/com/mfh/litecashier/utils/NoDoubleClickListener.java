package com.mfh.litecashier.utils;

import android.view.View;

import java.util.Calendar;

/**
 * Created by iris_wang on 2017/4/18.
 */

public abstract class NoDoubleClickListener implements View.OnClickListener {

    public static final int MIN_CLICK_DELAY_TIME = 500;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime < lastClickTime) {
            lastClickTime = 0;
        }
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onClickOnce(v);
        }
    }

    public void onClickOnce(View v) {

    }
}
