package com.mfh.litecashier.ui.fragment.purchase.intelligent;

import com.mfh.framework.api.invSendOrder.InvSendOrderItemBrief;
import com.mfh.framework.api.invSkuStore.InvSkuStoreMode;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 智能订货
 * Created by bingshanguxue on 16/3/17.
 */
public class IntelligentPurchasePresenter {
    private IIntelligentPurchaseView mIIntelligentPurchaseView;
    private InvSkuStoreMode mInvSkuStoreMode;

    //
    public IntelligentPurchasePresenter(IIntelligentPurchaseView intelligentPurchaseView) {
        this.mIIntelligentPurchaseView = intelligentPurchaseView;
        this.mInvSkuStoreMode = new InvSkuStoreMode();
    }
//

    /**
     * 加载智能订货商品
     */
    public void loadGoodsList(Long chainCompanyId) {
        mInvSkuStoreMode.autoAskSendOrder(chainCompanyId, new OnModeListener<InvSendOrderItemBrief>() {
            @Override
            public void onProcess() {
                if (mIIntelligentPurchaseView != null) {
                    mIIntelligentPurchaseView.onIntelligentPurchaseProcess();
                }
            }

            @Override
            public void onSuccess(InvSendOrderItemBrief data) {
                if (mIIntelligentPurchaseView != null) {
                    mIIntelligentPurchaseView.onIntelligentPurchaseSuccess(data != null ? data.getItems() : null);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIIntelligentPurchaseView != null) {
                    mIIntelligentPurchaseView.onIntelligentPurchaseError(errorMsg);
                }
            }
        });
    }
}
