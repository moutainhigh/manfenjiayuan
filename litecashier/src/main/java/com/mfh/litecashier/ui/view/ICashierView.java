package com.mfh.litecashier.ui.view;

import com.mfh.framework.mvp.MvpView;
import com.mfh.litecashier.database.entity.PosProductEntity;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface ICashierView extends MvpView {
    /**
     * 查询到商品
     * */
    void onFindGoods(PosProductEntity goods, int packFlag);

    /**
     * 未找到商品
     * */
    void onFindGoodsEmpty(String barcode);
}
