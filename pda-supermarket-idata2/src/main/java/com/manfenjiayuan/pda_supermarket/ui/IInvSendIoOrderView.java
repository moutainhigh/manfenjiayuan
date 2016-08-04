package com.manfenjiayuan.pda_supermarket.ui;

import com.mfh.framework.mvp.MvpView;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSendIoOrderView extends MvpView {
    void onIInvSendIoOrderViewProcess();
    void onIInvSendIoOrderViewError(String errorMsg);
    void onIInvSendIoOrderViewSuccess(InvSendIoOrderItemBrief data);
}
