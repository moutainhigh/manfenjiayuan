package com.mfh.litecashier.ui.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.mfh.litecashier.bean.PosOrder;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface IOrderflowView extends MvpView {
    void onProcess();

    void onError(String errorMsg);

    void onSuccess(PageInfo pageInfo, List<PosOrder> dataList);
}
