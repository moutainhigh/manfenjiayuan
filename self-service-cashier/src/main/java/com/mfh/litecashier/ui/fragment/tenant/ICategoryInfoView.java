package com.mfh.litecashier.ui.fragment.tenant;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 27/11/2016.
 */

public interface ICategoryInfoView extends MvpView {
    void onICategoryInfoViewProcess();
    void onICategoryInfoViewError(String errorMsg);
    void onICategoryInfoViewSuccess(PageInfo pageInfo, List<CategoryInfo> dataList);
}
