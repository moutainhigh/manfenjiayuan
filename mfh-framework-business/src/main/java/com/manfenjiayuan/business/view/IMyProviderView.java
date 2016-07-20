package com.manfenjiayuan.business.view;

import com.manfenjiayuan.business.bean.MyProvider;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 供应商
 * Created by bingshanguxue on 16/3/17.
 */
public interface IMyProviderView extends MvpView {
    void onProcess();
    void onError(String errorMsg);
    void onSuccess(PageInfo pageInfo, List<MyProvider> dataList);
}
