package com.mfh.litecashier.ui.view;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 采购商品
 * Created by bingshanguxue on 16/3/17.
 */
public interface IPurchaseView extends MvpView {
    void onLoadPurchaseGoodsProcess();
    void onLoadPurchaseGoodsError(String errorMsg);
    void onLoadPurchaseGoodsFinished(PageInfo pageInfo, List<ScGoodsSku> dataList);
}
