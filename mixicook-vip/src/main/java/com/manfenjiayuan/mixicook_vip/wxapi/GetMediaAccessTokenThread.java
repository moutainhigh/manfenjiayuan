package com.manfenjiayuan.mixicook_vip.wxapi;

import com.mfh.framework.anlaysis.logger.ZLogger;

import net.sourceforge.simcpux.wxapi.Constants;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 利用微信的OpenAPI获取MediaAccessToken，用于后面的上传录音文件
 * */
public class GetMediaAccessTokenThread extends Thread {

        final static String TAG = "GetMediaAccessTokenThread";
        private static final String URL_MEDIA_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

        @Override
        public void run() {

            StringBuffer stringBuffer;
            do {
                try {
                    HttpsURLConnection con = (HttpsURLConnection) new URL(String.format(URL_MEDIA_TOKEN,
                            new Object[]{Constants.APP_ID, Constants.APP_SECRET}))
                            .openConnection();
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    stringBuffer = new StringBuffer();
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null)
                            break;
                        stringBuffer.append(line);
                    }
                    String accessToken = ((JSONObject) new JSONTokener(stringBuffer.toString()).nextValue()).getString("access_token");
                    ZLogger.w("Media Access Token : " + accessToken);

                    new UploadMediaThread(accessToken).start();
                } catch (Exception exception) {
                    ZLogger.e("Exception in GetHttps : " + exception.getMessage());
                    return;
                }
            } while (stringBuffer.toString() == null);

        }
    }