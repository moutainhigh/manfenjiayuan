package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.api.invSkuStore.InvSkuStoreMode;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSkuGoodsPresenter {
    private IInvSkuGoodsView mInvSkuGoodsView;
    private InvSkuStoreMode mInvSkuGoodsMode;

    public InvSkuGoodsPresenter(IInvSkuGoodsView mInvSkuGoodsView) {
        this.mInvSkuGoodsView = mInvSkuGoodsView;
        this.mInvSkuGoodsMode = new InvSkuStoreMode();
    }

    /**
     * 查询商品
     * @param barcode 类目编号
     * */
    public void getByBarcodeMust(String barcode){
        mInvSkuGoodsMode.getByBarcodeMust(barcode, new OnModeListener<InvSkuGoods>() {
            @Override
            public void onProcess() {
                if (mInvSkuGoodsView != null) {
                    mInvSkuGoodsView.onIInvSkuGoodsViewProcess();
                }
            }

            @Override
            public void onSuccess(InvSkuGoods data) {
                if (mInvSkuGoodsView != null) {
                    mInvSkuGoodsView.onIInvSkuGoodsViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mInvSkuGoodsView != null) {
                    mInvSkuGoodsView.onIInvSkuGoodsViewError(errorMsg);
                }
            }
        });
    }



}
