package com.manfenjiayuan.business.view;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface IWholesalerView extends MvpView {
    String getShortCodeLike();
    void onProcess();
    void onError(String errorMsg);
    void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList);
}
