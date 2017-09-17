package com.manfenjiayuan.business.mvp.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSendOrderView extends MvpView {
    void onIInvSendOrderViewProcess();
    void onIInvSendOrderViewError(String errorMsg);
    void onIInvSendOrderViewSuccess(PageInfo pageInfo, List<InvSendOrder> dataList);
    void onIInvSendOrderViewItemsSuccess(List<InvSendOrderItem> items);
}
