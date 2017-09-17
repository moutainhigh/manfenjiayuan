package com.manfenjiayuan.mixicook_vip.wxapi;

import android.content.Context;
import android.os.SystemClock;

import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

import net.sourceforge.simcpux.WXHelper;
import net.sourceforge.simcpux.wxapi.Constants;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by bingshanguxue on 16/08/2017.
 */

public class MMVoiceSendManager {
    private static final String URL_MEDIA_TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String URL_UPLOAD_MEDIA = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=voice";

    public String accessToken;
    public String mediaAccessToken;
    public String mediaId;
    public String toUserId;
    public String fromUserId;
    public String voicePath;

    private static MMVoiceSendManager instance;

    public static MMVoiceSendManager getInstance() {
        if (instance == null) {
            synchronized (MMVoiceSendManager.class) {
                if (instance == null) {
                    instance = new MMVoiceSendManager();
                }
            }
        }
        return instance;
    }

    //利用微信的OpenAPI获取MediaAccessToken，用于后面的上传录音文件
    public void step1(final String path) {
        this.voicePath = path;

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZLogger.d("利用微信的OpenAPI获取MediaAccessToken，用于后面的上传录音文件");
                StringBuffer stringBuffer;
                String mediaAccessToken = null;
                do {
                    try {
                        String url = String.format(URL_MEDIA_TOKEN,
                                new Object[]{Constants.APP_ID, Constants.APP_SECRET});
                        ZLogger.i("step1: " + url);
                        HttpsURLConnection con = (HttpsURLConnection) new URL(url)
                                .openConnection();
                        con.setDoOutput(true);
                        con.setDoInput(true);
                        con.connect();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        stringBuffer = new StringBuffer();
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            stringBuffer.append(line);
                        }
                        mediaAccessToken = ((JSONObject) new JSONTokener(stringBuffer.toString()).nextValue()).getString("access_token");
                        ZLogger.w("Media Access Token : " + mediaAccessToken);

                    } catch (Exception exception) {
                        ZLogger.e("Exception in GetHttps : " + exception.getMessage());
                        break;
                    }
                } while (stringBuffer.toString() == null);

                if (!StringUtils.isEmpty(mediaAccessToken)) {
                    ZLogger.d("mediaAccessToken: " + mediaAccessToken);
                    step2(mediaAccessToken);
                } else {
                    ZLogger.w("find no mediaAccessToken");
                }
            }
        }).start();
    }


    public void step11(final String path) {
        this.voicePath = path;

        Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                ZLogger.d("利用微信的OpenAPI获取MediaAccessToken，用于后面的上传录音文件");
                StringBuffer stringBuffer;
                String accessToken = null;
                do {
                    try {
                        String url = String.format(URL_MEDIA_TOKEN,
                                new Object[]{Constants.APP_ID, Constants.APP_SECRET});
                        ZLogger.i("step1: " + url);
                        HttpsURLConnection con = (HttpsURLConnection) new URL(url)
                                .openConnection();
                        con.setDoOutput(true);
                        con.setDoInput(true);
                        con.connect();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        stringBuffer = new StringBuffer();
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                            stringBuffer.append(line);
                        }
                        accessToken = ((JSONObject) new JSONTokener(stringBuffer.toString()).nextValue()).getString("access_token");
                        ZLogger.w("Media Access Token : " + accessToken);

                    } catch (Exception exception) {
                        ZLogger.e("Exception in GetHttps : " + exception.getMessage());
                        break;
                    }
                } while (stringBuffer.toString() == null);

                ZLogger.d("accessToken=" + accessToken);
                subscriber.onNext(accessToken);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e(e.toString());
                    }

                    @Override
                    public void onNext(String mediaAccessToken) {
                        if (!StringUtils.isEmpty(mediaAccessToken)) {
                            ZLogger.d("mediaAccessToken: " + mediaAccessToken);
                            step2(mediaAccessToken);
                        } else {
                            ZLogger.w("find no mediaAccessToken");
                        }

                    }

                });
    }

    private void step2(final String mediaAccessToken) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String str = "--" + "---part---" + "\r\n";
                String url = String.format(URL_UPLOAD_MEDIA, new Object[]{mediaAccessToken});
                ZLogger.e("step2: " + url);
                int length;
                String mediaID;
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setReadTimeout(10000);
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setRequestProperty("Connection", "keep-alive");
                    con.setRequestProperty("Charsert", "UTF-8");
                    con.setRequestProperty("Content-type", "multipart/form-data;boundary=" + "---part---");
                    File voiceFile = new File(voicePath);//new File("/");
                    if ((voiceFile == null) || (!((File) voiceFile).exists()) || (!((File) voiceFile).isFile())) {
                        ZLogger.e("voiceFile error");
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
                        if (length == -1) {
                            break;
                        }
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
                        if (line == null) {
                            break;
                        }
                        stringBuffer.append(line);
                    }
                    bufferedReader.close();
                    ZLogger.e("uploadVoice : " + stringBuffer.toString());
                    JSONObject resultJSON = (JSONObject) new JSONTokener(stringBuffer.toString()).nextValue();
                    if (resultJSON.isNull("media_id") && !resultJSON.isNull("errcode")) {
                        ZLogger.e("uploadVoice Error: " + resultJSON.toString());
                        return;
                    }
                    mediaID = resultJSON.getString("media_id");
                    ZLogger.e("uploadVoice MediaID: " + mediaID);

                } catch (Exception exception) {
                    ZLogger.e("Exception in GetHttps : " + exception.getMessage());
                    return;
                }

                if (!StringUtils.isEmpty(mediaID)) {
                    ZLogger.i("mediaID=" + mediaID);

                    step3(mediaID);
                } else {
                    ZLogger.w("find no mediaID");
                }
            }
        }).start();

    }

    private void step22(final String mediaAccessToken) {
        Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String str = "--" + "---part---" + "\r\n";
                String url = String.format(URL_UPLOAD_MEDIA, new Object[]{mediaAccessToken});
                ZLogger.e("step2: " + url);
                int length;
                String mediaID;
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setReadTimeout(10000);
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setRequestProperty("Connection", "keep-alive");
                    con.setRequestProperty("Charsert", "UTF-8");
                    con.setRequestProperty("Content-type", "multipart/form-data;boundary=" + "---part---");
                    File voiceFile = new File(voicePath);//new File("/");
                    if ((voiceFile == null) || (!((File) voiceFile).exists()) || (!((File) voiceFile).isFile())) {
                        ZLogger.e("voiceFile error");
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
                        if (length == -1) {
                            break;
                        }
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
                        if (line == null) {
                            break;
                        }
                        stringBuffer.append(line);
                    }
                    bufferedReader.close();
                    ZLogger.e("uploadVoice : " + stringBuffer.toString());
                    JSONObject resultJSON = (JSONObject) new JSONTokener(stringBuffer.toString()).nextValue();
                    if (resultJSON.isNull("media_id") && !resultJSON.isNull("errcode")) {
                        ZLogger.e("uploadVoice Error: " + resultJSON.toString());
                        return;
                    }
                    mediaID = resultJSON.getString("media_id");
                    ZLogger.e("uploadVoice MediaID: " + mediaID);

                } catch (Exception exception) {
                    ZLogger.e("Exception in GetHttps : " + exception.getMessage());
                    return;
                }

                subscriber.onNext(mediaID);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.e(e.toString());
                    }

                    @Override
                    public void onNext(String mediaID) {
                        if (!StringUtils.isEmpty(mediaID)) {
                            ZLogger.i("mediaID=" + mediaID);

                            step3(mediaID);
                        } else {
                            ZLogger.w("find no mediaID");
                        }

                    }

                });
    }

    private void step3(String mediaId) {
        ZLogger.e("step3: mediaId=" + mediaId);
        new SendVoiceThread(accessToken, fromUserId, mediaId).start();
    }

}
