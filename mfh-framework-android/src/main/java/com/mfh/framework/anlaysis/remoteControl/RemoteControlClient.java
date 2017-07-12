package com.mfh.framework.anlaysis.remoteControl;

import android.os.Build;

import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.config.UConfig;
import com.mfh.framework.BizConfig;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.StringUtils;
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
        remoteControls.add(new RemoteControl(1L, "一键反馈", "手动反馈"));
        remoteControls.add(new RemoteControl(2L, "一键反馈", "远程反馈"));
        remoteControls.add(new RemoteControl(3L, "备份数据库", "备份数据库"));
        remoteControls.add(new RemoteControl(4L, "软件更新", "检查软件版本更新"));
        remoteControls.add(new RemoteControl(20L, "远程打印", "远程打印票据"));
        return remoteControls;
    }

    /**
     * 远程上传日志文件
     */
    public void remoteFeedback() {
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
                            ZLogger.ef("远程一键反馈:" + e.toString());
                            uploadLogFileStep2("远程反馈失败:" + e.toString(), null);
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);
                            uploadLogFileStep2("远程反馈成功", data);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }

    public void onekeyFeedback() {
        try {
//            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
//                    FileUtil.getSaveFile("", "onekeyfeedback"));

            File zipFile = FileUtil.getSaveFile("", "onekeyfeedback.zip");
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
                    zipFile);
//            String time = ZLogger.DATE_FORMAT.format(new Date());
//            String fileName = time + ".log";
//            ZipUtils.zipFile(FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName), zipFile);

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
                            ZLogger.ef("远程一键反馈:" + e.toString());
                            uploadLogFileStep2("远程反馈失败:" + e.toString(), null);
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);
                            uploadLogFileStep2("远程反馈成功", data);
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
    public static void uploadLogFileStep2(String feedback, Long attachmentId) {

        Map<String, String> options = new HashMap<>();

        JSONObject jsonObject = new JSONObject();

        JSONObject stackObject = new JSONObject();
        stackObject.put("terminalId", SharedPrefesManagerFactory.getTerminalId());
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(feedback)) {
            sb.append("\n").append(feedback);
        }
        if (attachmentId != null) {
            sb.append(String.format("\n附件编号： %d",
                    attachmentId));
        }
        stackObject.put("feedback", sb.toString());
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
                        ZLogger.ef(e.toString());
                    }

                    @Override
                    public void onValue(Long data) {
                        super.onValue(data);
                        ZLogger.d("一键反馈成功：" + data);
                    }
                });

    }

    /**拷贝数据库*/
    public void copyDatabase() {
        try {
//            String dbPath = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
//                    UConfig.CONFIG_PARAM_DB_PATH);
            String dbName;
            if (BizConfig.RELEASE){
                dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                        UConfig.CONFIG_PARAM_DB_NAME, "mfh_release.db");
            }
            else{
                dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                        "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_dev.db");
            }

            File dbFile = MfhApplication.getAppContext().getDatabasePath(dbName).getAbsoluteFile();
            File dbBackupFile = FileUtil.getSaveFile("",
                    String.format("%s_cashier_database_backup.zip", SharedPrefesManagerFactory.getTerminalId()));
            if (!dbBackupFile.exists()) {
                dbBackupFile.createNewFile();
            }

            ZLogger.d(String.format("准备压缩 %s 到 %s", dbFile.getPath(), dbBackupFile.getPath()));
            ZipUtils.zipFile(dbFile, dbBackupFile);

            ResHttpManager.getInstance().upload2(dbBackupFile,
                    new MValueSubscriber<Long>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            //retrofit2.adapter.rxjava.HttpException: HTTP 413 Request Entity Too Large
                            ZLogger.ef("拷贝数据库失败:" + e.toString());
                            uploadLogFileStep2("拷贝数据库失败:" + e.toString(), null);
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);
                            uploadLogFileStep2("拷贝数据库成功", data);
                        }
                    });

        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }
}
