package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 商城订单
 * Created by bingshanguxue on 16/3/21.
 */
public interface IScOrderView extends MvpView {
    void onIScOrderViewProcess();
    void onIScOrderViewError(String errorMsg);
    void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList);
    void onIScOrderViewSuccess(ScOrder data);
}
