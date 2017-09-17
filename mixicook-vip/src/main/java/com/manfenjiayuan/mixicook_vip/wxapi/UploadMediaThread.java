package com.manfenjiayuan.mixicook_vip.wxapi;


import com.mfh.framework.anlaysis.logger.ZLogger;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 获取MediaAccessToken后，上传录音文件，获取MediaID
 * */
public class UploadMediaThread extends Thread {

    String accessToken = null;
    final static String TAG = "UploadMediaThread";
    private static final String URL_UPLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=voice";

    public UploadMediaThread(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        String str = "--" + "---part---" + "\r\n";
        String url = String.format(URL_UPLOAD_MEDIA, new Object[]{accessToken});
        ZLogger.e(TAG, "uploadVoice http = " + url);
        int length;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setReadTimeout(10000);
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Charsert", "UTF-8");
            con.setRequestProperty("Content-type", "multipart/form-data;boundary=" + "---part---");
            File voiceFile = new File("/");
            if ((voiceFile == null) || (!((File) voiceFile).exists()) || (!((File) voiceFile).isFile())) {
                ZLogger.e(TAG, "voiceFile error");
                return;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("--" + str + "\r\n");
            stringBuilder.append("Content-Disposition: form-data;name=\"voice\";filename=\"" + voiceFile.getName() + "\";filelength=\"" + voiceFile.length() + "\"\r\n");
            stringBuilder.append("Content-Type: application/octet-stream; \r\n\r\n");
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(stringBuilder.toString().getBytes());
            FileInputStream inputStream = new FileInputStream(voiceFile);
            byte bytes[] = new byte[1024];
            while (true) {
                length = inputStream.read(bytes);
                if (length == -1)
                    break;
                outputStream.write(bytes, 0, length);
            }

            inputStream.close();
            outputStream.write("\r\n".getBytes());
            outputStream.write(("--" + str + "--\r\n").getBytes());
            outputStream.flush();
            outputStream.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                stringBuffer.append(line);
            }
            bufferedReader.close();
            ZLogger.e(TAG, "uploadVoice : " + stringBuffer.toString());
            JSONObject resultJSON = (JSONObject) new JSONTokener(stringBuffer.toString()).nextValue();
            if (resultJSON.isNull("media_id") && !resultJSON.isNull("errcode")) {
                ZLogger.e(TAG, "uploadVoice Error: " + resultJSON.toString());
                return;
            }
            String mediaID = resultJSON.getString("media_id");
            ZLogger.e(TAG, "uploadVoice MediaID: " + mediaID);

            new SendVoiceThread("wH7kOhXMJErwMCRjVEq5C9gllH5ZkRM04zqhxhHpYvD2W2f3YylwZDe1tAn0zH_p-ROU3hEDFwwsw-cHSIVFPbwVTrvq6yqjJr7F9xXdkJs", "o-aJaw1ovSKh9tlMdWnFA0PswdgU", mediaID).start();

        } catch (Exception exception) {
            ZLogger.e(TAG, "Exception in GetHttps : " + exception.getMessage());
            return;
        }
    }

}