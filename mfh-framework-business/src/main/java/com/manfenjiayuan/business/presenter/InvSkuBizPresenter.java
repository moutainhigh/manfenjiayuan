package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IInvSkuBizView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invSkuStore.InvSkuBizBean;
import com.mfh.framework.api.invSkuStore.InvSkuStoreMode;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商超库存商品
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSkuBizPresenter {
    private IInvSkuBizView mIScGoodsSkuView;
    private InvSkuStoreMode mInvSkuStoreMode;

    public InvSkuBizPresenter(IInvSkuBizView mIScGoodsSkuView) {
        this.mIScGoodsSkuView = mIScGoodsSkuView;
        this.mInvSkuStoreMode = new InvSkuStoreMode();
    }

    public void getBeanByBizKeys(String barcode) {
        mInvSkuStoreMode.getBeanByBizKeys(barcode, new OnModeListener<InvSkuBizBean>() {
            @Override
            public void onProcess() {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewProcess();
                }
            }

            @Override
            public void onSuccess(InvSkuBizBean data) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewError(errorMsg);
                }
            }

        });
    }

    public void listBeans(String skuName, PageInfo pageInfo) {
        mInvSkuStoreMode.listBeans(skuName, pageInfo, new OnPageModeListener<InvSkuBizBean>() {
            @Override
            public void onProcess() {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<InvSkuBizBean> dataList) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewSuccess(pageInfo, dataList);
                }
            }


            @Override
            public void onError(String errorMsg) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIInvSkuBizViewError(errorMsg);
                }
            }

        });
    }


}
