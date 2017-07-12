package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;
import com.mfh.framework.rxapi.bean.CommonAccountFlow;

import java.util.List;

/**
 * 库存盘点
 * Created by bingshanguxue on 16/3/21.
 */
public interface ICommonAccountHistoryView extends MvpView {
    void onICommonAccountHistoryProcess();
    void onICommonAccountHistoryError(String errorMsg);
    void onICommonAccountHistorySuccess(PageInfo pageInfo, List<CommonAccountFlow> dataList);
}
