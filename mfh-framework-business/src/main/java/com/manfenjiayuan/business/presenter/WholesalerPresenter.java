package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.MyProvider;
import com.manfenjiayuan.business.mode.WholesalerMode;
import com.manfenjiayuan.business.view.IMyProviderView;
import com.manfenjiayuan.business.view.IWholesalerView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 批发商
 * Created by bingshanguxue on 16/3/17.
 */
public class WholesalerPresenter {
    private IWholesalerView iWholesalerView;
    private IMyProviderView mIMyProviderView;
    private WholesalerMode iWholesalerMode;

    public WholesalerPresenter(IWholesalerView iTenantView) {
        this.iWholesalerView = iTenantView;
        this.iWholesalerMode = new WholesalerMode();
    }

    public WholesalerPresenter(IMyProviderView mIMyProviderView) {
        this.mIMyProviderView = mIMyProviderView;
        this.iWholesalerMode = new WholesalerMode();
    }

    /**
     * 获取门店批发商
     * */
    public void getWholesalers(String abilityItem, PageInfo pageInfo, String shortCodeLike){
        iWholesalerMode.getWholesalers(abilityItem, pageInfo, shortCodeLike, new OnPageModeListener<CompanyInfo>() {
            @Override
            public void onProcess() {
                if (iWholesalerView != null){
                    iWholesalerView.onProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
                if (iWholesalerView != null){
                    iWholesalerView.onSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iWholesalerView != null){
                    iWholesalerView.onError(errorMsg);
                }
            }
        });
    }

    /**
     * 获取批发商私有供应商
     * */
    public void invCompProviderFindMyProviders(PageInfo pageInfo){
        iWholesalerMode.invCompProviderFindMyProviders(pageInfo, new OnPageModeListener<MyProvider>() {
            @Override
            public void onProcess() {
                if (mIMyProviderView != null){
                    mIMyProviderView.onProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<MyProvider> dataList) {
                if (mIMyProviderView != null){
                    mIMyProviderView.onSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIMyProviderView != null){
                    mIMyProviderView.onError(errorMsg);
                }
            }
        });

    }
}
