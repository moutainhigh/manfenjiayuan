package com.manfenjiayuan.business.mvp.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.sc.product.ScProduct;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IScProductView extends MvpView {
    void onIScProductViewProcess();
    void onIScProductViewError(String errorMsg);
    void onIScProductViewSuccess(PageInfo pageInfo, List<ScProduct> dataList);
    void onIScProductViewSuccess(ScProduct data);
}
