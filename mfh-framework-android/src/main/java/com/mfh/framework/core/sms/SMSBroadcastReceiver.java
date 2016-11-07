package com.mfh.framework.core.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.core.utils.VersionUtils;

import java.util.Date;


/**
 * 11-07 12:43:18.078 5071-10468/? W/BroadcastQueue: Permission Denial: receiving Intent { act=android.provider.Telephony.SMS_RECEIVED flg=0x8000010 (has extras) } to com.manfenjiayuan.mixicook_vip/com.mfh.framework.core.sms.SMSBroadcastReceiver requires android.permission.RECEIVE_SMS due to sender com.android.phone (uid 1001)
 * Created by shengkun on 15/6/11.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    // Get the object of SmsManager
    final SmsManager mSmsManager = SmsManager.getDefault();

    Intent mServiceIntent;
//    WeakHandler mHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        //从Intent中接受信息
        Bundle bundle = intent.getExtras();
        if (bundle == null){
            return;
        }
        ZLogger.d("收到短信息" + StringUtils.decodeBundle(bundle));


        Object[] pdus = (Object[]) bundle.get("pdus");
        for (Object p : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) p);
            //获取短信内容
            final String content = message.getMessageBody();
            //获取发送时间
            final Date date = new Date(message.getTimestampMillis());
            final String sender = message.getOriginatingAddress();
            ZLogger.d("content=" + content);
            ZLogger.d("date=" + TimeUtil.FORMAT_MMDDHHMM.format(date));
            ZLogger.d("sender=" + sender);

//            if (!RegularUtils.isMobile(sender)) {
//                return;
//            }

            boolean isCpatchasMessage = CaptchaUtils.isCaptchasMessage(content);
            String captcha = CaptchaUtils.tryToGetCaptchas(content);

            if (isCpatchasMessage && !StringUtils.isEmpty(captcha)) {
                this.abortBroadcast();

                Message smsMessage = new Message();
                smsMessage.setMessage(true);
                smsMessage.setContent(content);
                smsMessage.setSender(sender);
                smsMessage.setDate(date);
                String company = SmsUtils.getContentInBracket(content, sender);
                if (company != null) {
                    smsMessage.setCompanyName(company);
                }
                //格式化短信日期提示
                //获得短信的各项内容
                String date_mms = TimeUtil.FORMAT_MMDDHHMM.format(date);
                smsMessage.setReceiveDate(date_mms);
                smsMessage.setReadStatus(0);
                smsMessage.setFromSmsDB(1);
                String captchas = CaptchaUtils.tryToGetCaptchas(content);
                if (!captchas.equals("")) {
                    smsMessage.setCaptchas(captchas);
                }
                String resultContent = SmsUtils.getResultText(smsMessage, false);
                if (resultContent != null) {
                    smsMessage.setResultContent(resultContent);
                }
                if (!VersionUtils.IS_MORE_THAN_LOLLIPOP) {
//                        smsMessage.save();
                }
                mServiceIntent = new Intent(context, CaptchasService.class);
                Bundle value = new Bundle();
                value.putSerializable("message", smsMessage);
                mServiceIntent.putExtra("bundle", value);
                context.startService(mServiceIntent);
            }

        }
    }

}
