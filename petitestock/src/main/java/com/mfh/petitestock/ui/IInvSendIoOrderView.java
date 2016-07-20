package com.mfh.petitestock.ui;

import com.mfh.framework.mvp.MvpView;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSendIoOrderView extends MvpView {
    void onQueryInvSendIoOrderProcess();
    void onQueryInvSendIoOrderError(String errorMsg);
    void onQueryInvSendIoOrderSuccess(InvSendIoOrderItemBrief data);
}
