package com.manfenjiayuan.mixicook_vip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.manfenjiayuan.mixicook_vip.service.MainService;
import com.mfh.framework.anlaysis.logger.ZLogger;


/**
 * Created by bingshanguxue on 09/08/2017.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent localIntent = new Intent(context, MainService.class);
            localIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            localIntent.putExtra(MainService.EXTRA_BOOT_LAUNCH, true);
            context.startService(localIntent);
            ZLogger.i("bingshanguxue boot start...");
        }

    }
}
