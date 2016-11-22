package com.mfh.framework.api.clientLog;

import android.os.Build;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientLogMode {

    public void create(String stackInformation, String userName,
                       final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("get terminal id success:" + retStr);
                            SharedPrefesManagerFactory.setTerminalId(retStr);
                            if (listener != null) {
                                listener.onSuccess(retStr);
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("get terminal id failed(%s),please set manual", errMsg));
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        ClientLog clientLog = new ClientLog();
        clientLog.setSoftVersion(SystemUtils.getVersionName(MfhApplication.getAppContext()));
        clientLog.setAndroidLevel(String.format("%s(API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        clientLog.setStackInformation(stackInformation);
        clientLog.setHardwareInformation(SystemUtils.getMobileInfo().toJSONString());
        clientLog.setLoginName(userName);
        clientLog.setErrorTime(new Date());

        ClientLogApi.create(clientLog, responseCallback);
    }


    public void list(PageInfo pageInfo,final OnPageModeListener<ClientLog> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        ZLogger.d(String.format("加载日志列表:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ClientLog>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ClientLog> rs) {
                //此处在主线程中执行。
                List<ClientLog> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ClientLog> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载日志失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ClientLog.class, MfhApplication.getAppContext());

        ClientLogApi.list(pageInfo, queryRsCallBack);
    }
}