package com.mfh.framework.anlaysis.remoteControl;

import android.os.Build;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.api.clientLog.ClientLogApi;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.ZipUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.ResHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * */
    public List<RemoteControl> generateRemoteControls(){
        List<RemoteControl> remoteControls = new ArrayList<>();
        remoteControls.add(new RemoteControl(1L, "一键反馈", "一键反馈"));
        remoteControls.add(new RemoteControl(3L, "软件更新", "检查软件版本更新"));
        remoteControls.add(new RemoteControl(20L, "远程打印", "远程打印票据"));
        return remoteControls;
    }

    /**
     * 远程上传日志文件
     * */
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
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);
                            StringBuilder sb = new StringBuilder();
                            sb.append(String.format("\n" +
                                            "一键反馈:日志文件编号为 %d\n",
                                    data));
                            sb.append("备注:");
                            uploadLogFileStep2(sb.toString(), MfhLoginService.get().getLoginName());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }


    /**
     * 远程上传日志文件:提交后台资源文件编号
     * */
    public static void uploadLogFileStep2(String stackInformation, String userName){
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(errMsg);
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        ClientLog clientLog = new ClientLog();
        clientLog.setSoftVersion(SystemUtils.getVersionName(MfhApplication.getAppContext()));
        clientLog.setAndroidLevel(String.format("%s(API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        clientLog.setStackInformation(stackInformation);
        clientLog.setHardwareInformation(String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
        clientLog.setLoginName(userName);
        clientLog.setErrorTime(new Date());

        ClientLogApi.create(clientLog, responseCallback);
    }
}
