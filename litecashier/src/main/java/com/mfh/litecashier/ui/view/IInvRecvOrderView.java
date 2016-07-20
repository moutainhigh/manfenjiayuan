package com.mfh.litecashier.ui.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.manfenjiayuan.business.bean.InvSendIoOrder;
import com.manfenjiayuan.business.bean.InvSendIoOrderItem;

import java.util.List;

/**
 * 采购收货订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvRecvOrderView extends MvpView {
    void onQueryOrderProcess();
    void onQueryOrderError(String errorMsg);
    void onQueryOrderSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList);
    void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList);
}
