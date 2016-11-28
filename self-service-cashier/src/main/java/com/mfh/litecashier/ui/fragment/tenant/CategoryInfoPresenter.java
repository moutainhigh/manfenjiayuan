package com.mfh.litecashier.ui.fragment.tenant;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.category.ScCategoryInfoMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public class CategoryInfoPresenter {
    private ICategoryInfoView mICategoryInfoView;
    private ScCategoryInfoMode mScCategoryInfoMode;

    public CategoryInfoPresenter(ICategoryInfoView iCategoryInfoView) {
        this.mICategoryInfoView = iCategoryInfoView;
        this.mScCategoryInfoMode = new ScCategoryInfoMode();
    }

    /**
     * 获取门店
     */
    public void list(int domain, int cateType, int catePosition,
                                      int deep, Long tenantId, PageInfo pageInfo) {
        mScCategoryInfoMode.list(domain, cateType, catePosition,deep,tenantId,pageInfo,
                new OnPageModeListener<CategoryInfo>() {
            @Override
            public void onProcess() {
                if (mICategoryInfoView != null) {
                    mICategoryInfoView.onICategoryInfoViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<CategoryInfo> dataList) {
                if (mICategoryInfoView != null) {
                    mICategoryInfoView.onICategoryInfoViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mICategoryInfoView != null) {
                    mICategoryInfoView.onICategoryInfoViewError(errorMsg);
                }
            }
        });

    }

}
