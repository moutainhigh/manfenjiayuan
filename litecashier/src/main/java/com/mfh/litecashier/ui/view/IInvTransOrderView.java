package com.mfh.litecashier.ui.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.mfh.litecashier.bean.InvTransOrder;

import java.util.List;

/**
 * 库存调拨订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvTransOrderView extends MvpView {
    void onQueryOrderProcess();
    void onQueryOrderError(String errorMsg);
    void onQueryOrderSuccess(PageInfo pageInfo, List<InvTransOrder> dataList);
    void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList);
}
