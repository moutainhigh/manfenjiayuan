package com.mfh.litecashier.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.service.ValidateManager;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class DailysettleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ZLogger.d(">>启动日结判断");
        //启动验证
        ValidateManager.get().stepValidate(ValidateManager.STEP_HAVENOMENYEND);
    }
}
