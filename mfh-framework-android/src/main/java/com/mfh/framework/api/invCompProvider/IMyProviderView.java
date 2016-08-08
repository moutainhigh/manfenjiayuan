package com.mfh.framework.api.invCompProvider;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 供应商
 * Created by bingshanguxue on 16/3/17.
 */
public interface IMyProviderView extends MvpView {
    void onIMyProviderViewProcess();
    void onIMyProviderViewError(String errorMsg);
    void onIMyProviderViewSuccess(PageInfo pageInfo, List<MyProvider> dataList);
}
