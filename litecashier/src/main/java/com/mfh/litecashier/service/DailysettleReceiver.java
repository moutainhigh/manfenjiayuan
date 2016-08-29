package com.mfh.litecashier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.framework.anlaysis.logger.ZLogger;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class DailysettleReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        ZLogger.df("DailysettleReceiver--check dailysettle automatic.");
        ValidateManager.get().stepValidate(ValidateManager.STEP_VALIDATE_ANALYSISACCDATE_NEEDDATEEND);
    }
}
