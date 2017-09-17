package com.manfenjiayuan.business.mvp.view;

import com.manfenjiayuan.business.bean.InvSkuProvider;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IProviderGoodsSkuView extends MvpView {
    void onProcess();
    void onError(String errorMsg);
    void onSuccess(PageInfo pageInfo, List<InvSkuProvider> dataList);
}
