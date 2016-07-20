package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.InvSkuProvider;
import com.manfenjiayuan.business.mode.ProviderGoodsSkuMode;
import com.manfenjiayuan.business.view.IProviderGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ProviderGoodsSkuPresenter {
    private IProviderGoodsSkuView mIProviderGoodsSkuView;
    private ProviderGoodsSkuMode mProviderGoodsSkuMode;

    public ProviderGoodsSkuPresenter(IProviderGoodsSkuView mIProviderGoodsSkuView) {
        this.mIProviderGoodsSkuView = mIProviderGoodsSkuView;
        this.mProviderGoodsSkuMode = new ProviderGoodsSkuMode();
    }

    /**
     * 获取批发商商品
     * @param companyId 批发商编号
     * */
    public void loadSupplyGoods(PageInfo pageInfo, Long providerId, String barcode){
        mProviderGoodsSkuMode.listInvSkuProvider(pageInfo, providerId, barcode, new OnPageModeListener<InvSkuProvider>() {
            @Override
            public void onProcess() {
                if (mIProviderGoodsSkuView != null) {
                    mIProviderGoodsSkuView.onProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSkuProvider> dataList) {
                if (mIProviderGoodsSkuView != null) {
                    mIProviderGoodsSkuView.onSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIProviderGoodsSkuView != null) {
                    mIProviderGoodsSkuView.onError(errorMsg);
                }
            }
        });
    }

}
