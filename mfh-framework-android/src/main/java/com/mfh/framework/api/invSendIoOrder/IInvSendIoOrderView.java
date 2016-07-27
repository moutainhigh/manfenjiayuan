package com.mfh.framework.api.invSendIoOrder;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 收发单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSendIoOrderView extends MvpView {
    void onQueryOrderProcess();
    void onQueryOrderError(String errorMsg);
    void onQueryOrderSuccess(PageInfo pageInfo, List<InvSendIoOrder> dataList);
    void onQueryOrderItemsSuccess(List<InvSendIoOrderItem> dataList);
}
