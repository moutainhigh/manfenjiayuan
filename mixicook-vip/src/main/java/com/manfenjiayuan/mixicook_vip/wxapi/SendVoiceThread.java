package com.manfenjiayuan.mixicook_vip.wxapi;

import android.util.Log;


import com.mfh.framework.anlaysis.logger.ZLogger;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 获取MediaID后，直接发送给用户的OpenID：
 */
public class SendVoiceThread extends Thread {

    String accessToken = null;
    String toUserID = null;
    String mediaID = null;
    final static String TAG = "SendVoiceThread";
    private static final String URL_MESSAGE = "https://api.weixin.qq.com/sns/authorize/message?access_token=%s&openid=%s";

    public SendVoiceThread(String accessToken, String toUserID, String mediaID) {
        this.accessToken = accessToken;
        this.toUserID = toUserID;
        this.mediaID = mediaID;
    }

    @Override
    public void run() {
        String url = String.format(URL_MESSAGE, new Object[]{accessToken, toUserID});
        ZLogger.e("sendVoiceMsg https = " + (String) url);
        String toSendJSON;
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL((String) url).openConnection();
            con.setReadTimeout(8000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            toSendJSON = "{\"touser\":\"" + toUserID + "\",\"msgtype\":\"voice\",\"voice\":{\"media_id\":\"" + mediaID + "\"}}";
            ZLogger.e(TAG, "sendVoiceMsg json = " + toSendJSON);
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(toSendJSON.getBytes());
            outputStream.flush();
            outputStream.close();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = inputStream.readLine();
                if (line == null)
                    break;
                stringBuffer.append(line);
            }
            ZLogger.e(TAG, "sendVoiceMsg return : " + stringBuffer.toString());
            JSONObject resultJSON = (JSONObject) new JSONTokener(stringBuffer.toString()).nextValue();
            if (!resultJSON.isNull("errcode")) {
                int errCode = resultJSON.getInt("errcode");
            } else {
                Log.e(TAG, " >>> sendVoiceMsg Msg success. ");
            }
        } catch (Exception localException) {
            Log.e(TAG, "Exception in GetHttps : " + localException.getMessage());
            return;
        }
    }

}