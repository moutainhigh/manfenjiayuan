package com.mfh.framework.api.invCompProvider;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * Created by bingshanguxue on 7/27/16.
 */
public class InvComProviderPresenter {
    private IMyProviderView mIMyProviderView;
    private InvComProviderMode mInvComProviderMode;

    public InvComProviderPresenter(IMyProviderView iMyProviderView) {
        mIMyProviderView = iMyProviderView;
        mInvComProviderMode = new InvComProviderMode();
    }

    /**
     * 获取批发商私有供应商
     * */
    public void findMyProviders(PageInfo pageInfo){
        mInvComProviderMode.findMyProviders(pageInfo, new OnPageModeListener<MyProvider>() {
            @Override
            public void onProcess() {
                if (mIMyProviderView != null){
                    mIMyProviderView.onIMyProviderViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<MyProvider> dataList) {
                if (mIMyProviderView != null){
                    mIMyProviderView.onIMyProviderViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIMyProviderView != null){
                    mIMyProviderView.onIMyProviderViewError(errorMsg);
                }
            }
        });

    }
}
