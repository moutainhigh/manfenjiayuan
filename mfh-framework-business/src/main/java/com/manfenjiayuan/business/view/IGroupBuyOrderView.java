package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.mvp.MvpView;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;

import java.util.List;

/**
 * 库存盘点
 * Created by bingshanguxue on 16/3/21.
 */
public interface IGroupBuyOrderView extends MvpView {
    void onIGroupBuyOrderViewProcess();
    void onIGroupBuyOrderViewError(String errorMsg);
    void onQueryGroupBuyActitiySuccess(PageInfo pageInfo, List<GroupBuyActivity> dataList);
    void onQueryGroupBuyOrderSuccess(PageInfo pageInfo, List<GroupBuyOrder> dataList);
    void onIGroupBuyOrderViewSuccess(ScOrder data);
    void onNotifyTakeGoodsSuccess(String data);
    void onNotifyReceiveAndFinishOrderSuccess(String data);
}
