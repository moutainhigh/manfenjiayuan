package com.mfh.litecashier.ui.fragment.goods;

import com.mfh.framework.mvp.MvpView;

/**
 * 导入商品到前台类目
 *
 * Created by bingshanguxue on 01/04/2017.
 */

public interface IImportGoodsView extends MvpView {
    void onIImportGoodsViewProcess();
    void onIImportGoodsViewError(String errorMsg);
    void onIImportGoodsViewSuccess();

}
