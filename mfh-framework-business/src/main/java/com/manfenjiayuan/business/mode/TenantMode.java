package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class TenantMode implements ITenantMode<CompanyInfo> {

    @Override
    public void getTenants(PageInfo pageInfo, String nameLike, Integer abilityItem, final OnPageModeListener<CompanyInfo> listener) {
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
                ZLogger.d("加载关联租户失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, CompanyInfo.class, MfhApplication.getAppContext());

        CashierApiImpl.findPublicCompanyInfo(nameLike, abilityItem, pageInfo, queryRsCallBack);
    }
}
