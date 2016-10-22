package com.mfh.framework.anlaysis.remoteControl;

import android.os.Build;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.api.clientLog.ClientLogApi;
import com.mfh.framework.api.res.ResApi;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

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
        remoteControls.add(new RemoteControl(1L, "上传日志", "上传系统日志信息"));
        remoteControls.add(new RemoteControl(2L, "软件更新", "检查软件版本更新"));
        remoteControls.add(new RemoteControl(3L, "远程打印", "远程打印票据"));
        return remoteControls;
    }

    /**
     * 远程上传日志文件
     * */
    public void uploadLogFileStep1(){
        String time = ZLogger.DATE_FORMAT.format(new Date());
        String fileName = time + ".log";

        File file = FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName);
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Long,
                NetProcessor.Processor<Long>>(
                new NetProcessor.Processor<Long>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
//                        ScOrder scOrder = null;
                        if (rspData != null) {
                            RspValue<Long> retValue = (RspValue<Long>) rspData;
                            String stackTraceInfo = String.format("远程控制上传日志文件,文件编号为 %d",
                                    retValue.getValue());
                            uploadLogFileStep2(stackTraceInfo, MfhLoginService.get().getLoginName());
                        }
//                        if (listener != null) {
//                            listener.onSuccess(scOrder);
//                        }

                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询失败: " + errMsg);
//                        if (listener != null) {
//                            listener.onError(errMsg);
//                        }
                    }
                }
                , Long.class
                , MfhApplication.getAppContext()) {
        };

        ResApi.upload(file, responseCallback);
    }

    /**
     * 远程上传日志文件:提交后台资源文件编号
     * */
    private void uploadLogFileStep2(String stackInformation, String userName){
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
