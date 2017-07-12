package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.mfh.framework.rxapi.bean.GoodsOrder;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface IOrderflowView extends MvpView {
    void onProcess();

    void onError(String errorMsg);

    void onSuccess(PageInfo pageInfo, List<GoodsOrder> dataList);
}
