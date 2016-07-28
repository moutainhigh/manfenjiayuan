package com.mfh.litecashier.ui.fragment.purchase.intelligent;

import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 智能订货
 * Created by bingshanguxue on 16/3/17.
 */
public interface IIntelligentPurchaseView extends MvpView {
    void onIntelligentPurchaseProcess();

    void onIntelligentPurchaseError(String errorMsg);

    void onIntelligentPurchaseSuccess(List<InvSendOrderItem> dataList);
}
