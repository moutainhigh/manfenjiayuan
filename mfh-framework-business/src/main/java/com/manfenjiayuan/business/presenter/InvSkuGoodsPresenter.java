package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.mode.InvSkuGoodsMode;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSkuGoodsPresenter {
    private IInvSkuGoodsView mInvSkuGoodsView;
    private InvSkuGoodsMode mInvSkuGoodsMode;

    public InvSkuGoodsPresenter(IInvSkuGoodsView mInvSkuGoodsView) {
        this.mInvSkuGoodsView = mInvSkuGoodsView;
        this.mInvSkuGoodsMode = new InvSkuGoodsMode();
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
                    mInvSkuGoodsView.onProcess();
                }
            }

            @Override
            public void onSuccess(InvSkuGoods data) {
                if (mInvSkuGoodsView != null) {
                    mInvSkuGoodsView.onSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mInvSkuGoodsView != null) {
                    mInvSkuGoodsView.onError(errorMsg);
                }
            }
        });
    }



}
