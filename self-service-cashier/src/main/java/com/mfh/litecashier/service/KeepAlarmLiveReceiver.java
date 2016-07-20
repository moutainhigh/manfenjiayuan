package com.mfh.litecashier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.litecashier.utils.AlarmManagerHelper;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class KeepAlarmLiveReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            AlarmManagerHelper.registerDailysettle(context);
        }
    }
}
