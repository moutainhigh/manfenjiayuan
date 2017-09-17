package com.manfenjiayuan.business.mvp.view;

import com.mfh.framework.api.invFindOrder.InvFindOrderItemBrief;
import com.mfh.framework.mvp.MvpView;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvFindOrderView extends MvpView {
    void onQueryInvFindOrderProcess();
    void onQueryInvFindOrderError(String errorMsg);
    void onQueryInvFindOrderSuccess(InvFindOrderItemBrief data);
}
