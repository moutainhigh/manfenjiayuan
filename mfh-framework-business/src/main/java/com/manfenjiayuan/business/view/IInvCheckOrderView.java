package com.manfenjiayuan.business.view;

import com.mfh.framework.api.invCheckOrder.InvCheckOrder;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 库存盘点
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvCheckOrderView extends MvpView {
    void onIInvCheckOrderViewProcess();
    void onIInvCheckOrderViewError(String errorMsg);
    void onIInvCheckOrderViewSuccess(PageInfo pageInfo, List<InvCheckOrder> dataList);
    void onIInvCheckOrderViewSuccess(ScOrder data);
}
