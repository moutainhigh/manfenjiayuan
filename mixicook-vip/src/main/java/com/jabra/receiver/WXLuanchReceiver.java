package com.jabra.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.jabra.listener.WXLuanchListener;
import com.mfh.framework.anlaysis.logger.ZLogger;

public class WXLuanchReceiver
        extends BaseReceiver {
    private WXLuanchListener mWXLuanchListener;

    public void onReceive(Context context, Intent intent) {
        ZLogger.d(intent.getAction());
        if (this.mWXLuanchListener != null) {
            this.mWXLuanchListener.onWXLuanch();
        }
    }

    public void register(Context paramContext, WXLuanchListener paramWXLuanchListener) {
        this.mWXLuanchListener = paramWXLuanchListener;
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP");
        localIntentFilter.addCategory("com.tencent.mm.category.com.xpg.jabra.proto");
        paramContext.registerReceiver(this, localIntentFilter);
    }

    public void unregister(Context paramContext) {
        try {
            paramContext.unregisterReceiver(this);
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
}
