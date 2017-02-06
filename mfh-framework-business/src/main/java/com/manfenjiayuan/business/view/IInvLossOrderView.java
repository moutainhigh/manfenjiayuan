package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 库存盘点
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvLossOrderView extends MvpView {
    void onIInvLossOrderViewProcess();
    void onIInvLossOrderViewError(String errorMsg);
    void onIInvLossOrderViewSuccess(PageInfo pageInfo, List<InvLossOrder> dataList);
    void onIInvLossOrderViewSuccess(ScOrder data);
}
