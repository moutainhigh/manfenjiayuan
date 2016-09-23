package com.bingshanguxue.almigod.clientLog;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

public interface IClientLogView extends MvpView {
    void onIClientLogViewProcess();
    void onIClientLogViewError(String errorMsg);
    void onIClientLogViewSuccess(PageInfo pageInfo, List<ClientLog> dataList);
}