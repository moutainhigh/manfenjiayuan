package com.mfh.framework.api.clientLog;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

public class ClientLogMode {

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