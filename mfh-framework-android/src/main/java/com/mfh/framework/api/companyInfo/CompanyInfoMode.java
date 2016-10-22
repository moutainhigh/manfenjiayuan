package com.mfh.framework.api.companyInfo;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.netInfo.NetInfoApi;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class CompanyInfoMode {

    public void findPublicCompanyInfo(PageInfo pageInfo, String nameLike, Integer abilityItem,
                                      final OnPageModeListener<CompanyInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        ZLogger.d(String.format("加载门店开始:page=%d/%d", pageInfo.getPageNo(), pageInfo.getTotalPage()));
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyInfo>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<CompanyInfo> rs) {
                //此处在主线程中执行。
                List<CompanyInfo> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<CompanyInfo> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("查询网点失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, CompanyInfo.class, MfhApplication.getAppContext());

        CompanyInfoApiImpl.findPublicCompanyInfo(nameLike, abilityItem, pageInfo, queryRsCallBack);
    }

    public void findServicedNetsForUserPos(Long cityId, String userLng, String userLat,
                                           PageInfo pageInfo,
                                      final OnPageModeListener<CompanyInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<CompanyInfo>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<CompanyInfo> rs) {
                //此处在主线程中执行。
                List<CompanyInfo> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<CompanyInfo> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("查询网点失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, CompanyInfo.class, MfhApplication.getAppContext());

        NetInfoApi.findServicedNetsForUserPos(cityId, userLng, userLat,pageInfo, queryRsCallBack);
    }

}
