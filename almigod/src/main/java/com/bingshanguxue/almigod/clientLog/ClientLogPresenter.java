package com.bingshanguxue.almigod.clientLog;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.api.clientLog.ClientLogMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

public class ClientLogPresenter {
    private IClientLogView mIClientLogView;
    private ClientLogMode mClientLogMode;

    public ClientLogPresenter(IClientLogView iClientLogView) {
        this.mIClientLogView = iClientLogView;
        this.mClientLogMode = new ClientLogMode();
    }

    /**
     * 获取门店
     */
    public void list(PageInfo pageInfo) {
        mClientLogMode.list(pageInfo,
                new OnPageModeListener<ClientLog>() {
            @Override
            public void onProcess() {
                if (mIClientLogView != null) {
                    mIClientLogView.onIClientLogViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ClientLog> dataList) {
                if (mIClientLogView != null) {
                    mIClientLogView.onIClientLogViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIClientLogView != null) {
                    mIClientLogView.onIClientLogViewError(errorMsg);
                }
            }
        });

    }
}