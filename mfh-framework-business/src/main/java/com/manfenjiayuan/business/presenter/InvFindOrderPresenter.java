package com.manfenjiayuan.business.presenter;

import com.mfh.framework.api.invFindOrder.InvFindOrderItemBrief;
import com.mfh.framework.api.invFindOrder.InvFindOrderMode;
import com.manfenjiayuan.business.view.IInvFindOrderView;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 拣货单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvFindOrderPresenter {
    private IInvFindOrderView mIInvFindOrderView;
    private InvFindOrderMode mInvFindOrderMode;

    public InvFindOrderPresenter(IInvFindOrderView mIInvFindOrderView) {
        this.mIInvFindOrderView = mIInvFindOrderView;
        this.mInvFindOrderMode = new InvFindOrderMode();
    }

    public void loadOrderItemsByBarcode(String barcode) {
        mInvFindOrderMode.loadOrderItemsByBarcode(barcode, new OnModeListener<InvFindOrderItemBrief>() {
            @Override
            public void onProcess() {
                if (mIInvFindOrderView != null) {
                    mIInvFindOrderView.onQueryInvFindOrderProcess();
                }
            }

            @Override
            public void onSuccess(InvFindOrderItemBrief data) {
                if (mIInvFindOrderView != null) {
                    mIInvFindOrderView.onQueryInvFindOrderSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIInvFindOrderView != null) {
                    mIInvFindOrderView.onQueryInvFindOrderError(errorMsg);
                }
            }
        });
    }


}
