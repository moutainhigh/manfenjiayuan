package com.mfh.framework.api.invCompProvider;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 7/27/16.
 */
public class InvComProviderMode {

    public void findMyProviders(PageInfo pageInfo,
                                               final OnPageModeListener<MyProvider> listener) {
        if (listener != null){
            listener.onProcess();
        }
        ZLogger.d(String.format("加载批发商供应商开始:page=%d/%d",
                pageInfo.getPageNo(), pageInfo.getTotalPage()));

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<MyProvider>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<MyProvider> rs) {
                //此处在主线程中执行。
                List<MyProvider> entityList = new ArrayList<>();
                if (rs != null){
                    for (EntityWrapper<MyProvider> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }

                if (listener != null){
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载批发商失败:" + errMsg);
                if (listener != null){
                    listener.onError(errMsg);
                }
            }
        }, MyProvider.class, MfhApplication.getAppContext());

        InvComProviderApi.findMyProviders(pageInfo, queryRsCallBack);
    }
}
