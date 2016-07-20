package com.manfenjiayuan.business.view;

import com.manfenjiayuan.business.bean.InvFindOrderItemBrief;
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
