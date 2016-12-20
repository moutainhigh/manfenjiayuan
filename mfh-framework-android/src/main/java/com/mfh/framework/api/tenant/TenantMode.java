package com.mfh.framework.api.tenant;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 网点
 * Created by bingshanguxue on 16/3/17.
 */
public class TenantMode {

    public void listWhole(int bizDomainType, int domainUrlType, PageInfo pageInfo,
                           final OnPageModeListener<TenantInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //回调
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<TenantInfo>(pageInfo) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<TenantInfo> rs) {//此处在主线程中执行。
                        //此处在主线程中执行。
                        List<TenantInfo> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<TenantInfo> wrapper : rs.getRowDatas()) {
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
                        ZLogger.d("加载网点失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , TenantInfo.class
                , MfhApplication.getAppContext());

        TenantApi.listWhole(bizDomainType, domainUrlType, pageInfo, responseCallback);
    }

    public void getSaasInfo(String url, Long id, final OnModeListener<SassInfo> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<SassInfo,
                NetProcessor.Processor<SassInfo>>(
                new NetProcessor.Processor<SassInfo>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                        try {
                            RspBean<SassInfo> retValue = (RspBean<SassInfo>) rspData;
                            if (listener != null) {
                                listener.onSuccess(retValue.getValue());
                            }
                        }
                        catch (Exception e){
                            ZLogger.ef(e.toString());
                            if (listener != null) {
                                listener.onError(e.getMessage());
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载店铺信息失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , SassInfo.class
                , MfhApplication.getAppContext()) {
        };

        TenantApi.getSaasInfo(url, id, responseCallback);
    }
}
