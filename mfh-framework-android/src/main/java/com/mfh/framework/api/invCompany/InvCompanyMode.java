package com.mfh.framework.api.invCompany;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 7/27/16.
 */
public class InvCompanyMode {

    public void list(PageInfo pageInfo, String shortCodeLike,
                               final OnPageModeListener<CompanyInfo> listener) {
        if (listener != null){
            listener.onProcess();
        }
        ZLogger.d(String.format("加载批发商开始:page=%d/%d",
                pageInfo.getPageNo(), pageInfo.getTotalPage()));

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyInfo>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<CompanyInfo> rs) {
                //此处在主线程中执行。
                List<CompanyInfo> entityList = new ArrayList<>();
                if (rs != null){
                    for (EntityWrapper<CompanyInfo> wrapper : rs.getRowDatas()) {
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
        }, CompanyInfo.class, MfhApplication.getAppContext());

        InvCompanyApi.list(InvCompanyApi.STATUS_ONLINE, shortCodeLike, pageInfo, queryRsCallBack);
    }
}
