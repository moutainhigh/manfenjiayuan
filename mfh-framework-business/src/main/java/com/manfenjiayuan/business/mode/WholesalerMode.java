package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.MyProvider;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.CashierApi;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 批发商,接口实现
 * Created by bingshanguxue on 16/3/17.
 */
public class WholesalerMode implements IWholesalerMode<CompanyInfo> {

    @Override
    public void getWholesalers(String abilityItem, PageInfo pageInfo, String shortCodeLike, final OnPageModeListener<CompanyInfo> listener) {
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

        CashierApiImpl.bizSupplyInfoFindPublicCompanyInfo(shortCodeLike, abilityItem, pageInfo, queryRsCallBack);
    }

    public void invCompProviderFindMyProviders(PageInfo pageInfo, final OnPageModeListener<MyProvider> listener) {
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

        AjaxParams params = new AjaxParams();
//        params.put("abilityItem", String.format("%d,%d", AbilityItem.PROVIDER, AbilityItem.CASCADE));//能力
        params.put("page", Integer.toString(pageInfo.getPageNo()));
        params.put("rows", Integer.toString(pageInfo.getPageSize()));
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(CashierApi.URL_INVCOMPPROVIDER_FINDMYPROVIDERS, params, queryRsCallBack);
    }

}
