package com.manfenjiayuan.mixicook_vip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.ArrayList;
import java.util.Iterator;

public class WXNotifyReceiver extends BroadcastReceiver {
    private static final String EXTRA_EXT_OPEN_NOTIFY_TYPE = "EXTRA_EXT_OPEN_NOTIFY_TYPE";
    private static final String EXTRA_EXT_OPEN_USER_DATA = "EXTRA_EXT_OPEN_USER_DATA";
    private static final String NEW_MESSAGE = "NEW_MESSAGE";

    public void onReceive(Context paramContext, Intent paramIntent) {
        if (paramContext == null || paramIntent == null) {
            ZLogger.e("param null");
            return;
        }

        ZLogger.e("new Message" + paramContext.toString() + "\n" + paramIntent.toString());
        String notifyType = paramIntent.getStringExtra(EXTRA_EXT_OPEN_NOTIFY_TYPE);
        if (notifyType == null || notifyType.length() < 0) {
            ZLogger.e("wrong intent extra notifyType");
            return;
        }

        if (NEW_MESSAGE.equalsIgnoreCase(notifyType)) {
            ArrayList<String> userData = paramIntent.getStringArrayListExtra("EXTRA_EXT_OPEN_USER_DATA");

            if ((userData == null) || (userData.size() <= 0)) {
                ZLogger.e("wrong intent extra userDatas");
                return;
            }

            ZLogger.e("notifyType = " + notifyType);
            Iterator<String> it = userData.iterator();

            //获取微信新消息中用户的OpenID
            while (it.hasNext()) {
                String paramString = (String) it.next();
                ZLogger.e("userData = " + paramString);
                String[] params = paramString.split(",");
                if ((params == null) || (params.length < 3)) {
                    ZLogger.e("wrong userData");
                    return;
                }

                ZLogger.e("receive wechat data ... ");
                ZLogger.e("openID:" + params[0]);
            }
        }
    }
}