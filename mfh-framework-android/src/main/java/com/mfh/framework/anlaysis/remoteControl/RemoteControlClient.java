package com.mfh.framework.anlaysis.remoteControl;

import android.os.Build;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.core.utils.ZipUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ClientLogHttpManager;
import com.mfh.framework.rxapi.http.ResHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 远程控制
 * Created by bingshanguxue on 9/30/16.
 */

public class RemoteControlClient {
    private static RemoteControlClient instance = null;

    /**
     * 返回 DataSyncManagerImpl 实例
     *
     * @return CloudSyncManager
     */
    public static RemoteControlClient getInstance() {
        if (instance == null) {
            synchronized (RemoteControlClient.class) {
                if (instance == null) {
                    instance = new RemoteControlClient();
                }
            }
        }
        return instance;
    }


    /**
     * 远程指令
     */
    public List<RemoteControl> generateRemoteControls() {
        List<RemoteControl> remoteControls = new ArrayList<>();
        remoteControls.add(new RemoteControl(1L, "一键反馈", "一键反馈"));
        remoteControls.add(new RemoteControl(3L, "软件更新", "检查软件版本更新"));
        remoteControls.add(new RemoteControl(20L, "远程打印", "远程打印票据"));
        return remoteControls;
    }

    /**
     * 远程上传日志文件
     */
    public void onekeyFeedback() {
        try {
//            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
//                    FileUtil.getSaveFile("", "onekeyfeedback"));

            File zipFile = FileUtil.getSaveFile("", "onekeyfeedback.zip");
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
//            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
//                    zipFile);
            String time = ZLogger.DATE_FORMAT.format(new Date());
            String fileName = time + ".log";
            ZipUtils.zipFile(FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName), zipFile);

            File file = FileUtil.getSaveFile("", "onekeyfeedback.zip");//FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName);
            if (!file.exists()) {
                return;
            }
            ZLogger.d("file: " + file.getPath());

            ResHttpManager.getInstance().upload2(file,
                    new MValueSubscriber<Long>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            //retrofit2.adapter.rxjava.HttpException: HTTP 413 Request Entity Too Large
                            ZLogger.ef("一键反馈:" + e.toString());
                            StringBuilder sb = new StringBuilder();
                            sb.append("一键反馈:文件上传失败\n");
                            sb.append("备注:");
                            uploadLogFileStep2(sb.toString());
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);
                            StringBuilder sb = new StringBuilder();
                            sb.append(String.format("\n" +
                                            "一键反馈:日志文件编号为 %d\n",
                                    data));
                            sb.append("备注:");
                            uploadLogFileStep2(sb.toString());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }


    /**
     * 远程上传日志文件:提交后台资源文件编号
     */
    public static void uploadLogFileStep2(String stackInformation) {

        Map<String, String> options = new HashMap<>();

        JSONObject jsonObject = new JSONObject();

        JSONObject stackObject = new JSONObject();
        stackObject.put("terminalId", SharedPrefesManagerFactory.getTerminalId());
        stackObject.put("feedback", stackInformation);
        jsonObject.put("stackInformation", stackObject.toJSONString());
        jsonObject.put("hardwareInformation", String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
        jsonObject.put("androidLevel", String.format("%s(API %d)",
                Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        jsonObject.put("loginName", MfhLoginService.get().getLoginName());
        jsonObject.put("softVersion", SystemUtils.getVersion(MfhApplication.getAppContext()));
        jsonObject.put("errorTime", TimeUtil.format(new Date(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));

        options.put("jsonStr", jsonObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ClientLogHttpManager.getInstance().create(options,
                new MValueSubscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df(e.toString());
                    }

                    @Override
                    public void onValue(Long data) {
                        super.onValue(data);
                        ZLogger.df("一键反馈成功：" + data);
                    }
                });

    }
}
