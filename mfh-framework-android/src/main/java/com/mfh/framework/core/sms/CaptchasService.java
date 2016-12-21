package com.mfh.framework.core.sms;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.mfh.framework.R;
import com.mfh.framework.core.utils.ClipboardUtils;
import com.mfh.framework.core.utils.DialogUtil;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by shengkun on 15/6/11.
 */
public class CaptchasService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            Bundle bundle = intent.getBundleExtra("bundle");
            Message message = (Message) bundle.getSerializable("message");

            if (message != null && message.getCaptchas() != null) {
                ClipboardUtils.copyText(CaptchasService.this, message.getCaptchas());
                // 弹两遍，加长时间。
                DialogUtil.showHint(getString(R.string.captcha_tip, message.getCaptchas()));
//                NotificationUtils.showMessageInNotificationBar(CaptchasService.this,
//                        message.getSender(), message.getContent());

                EventBus.getDefault().post(new CaptchasEvent(CaptchasEvent.EVENT_ID_RECEIVE_SMS, bundle));
            }
        }
        return START_STICKY;
    }
}
