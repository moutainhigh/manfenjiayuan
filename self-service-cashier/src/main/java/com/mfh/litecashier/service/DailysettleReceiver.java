package com.mfh.litecashier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.framework.core.logger.ZLogger;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class DailysettleReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        ZLogger.df(">>启动自动检查清分");
        ValidateManager.get().stepValidate(ValidateManager.STEP_VALIDATE_ANALYSISACCDATE_HAVEDATEEND);
    }
}
