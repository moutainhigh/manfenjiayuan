package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.ProductSku;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IScProcuctPriceView extends MvpView {
    void onScProcuctPriceViewProcess();
    void onScProcuctPriceViewError(String errorMsg);
    void onScProcuctPriceViewSuccess(PageInfo pageInfo, List<ProductSku> dataList);
}
