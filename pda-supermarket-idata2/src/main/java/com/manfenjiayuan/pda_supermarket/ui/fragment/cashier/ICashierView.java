package com.manfenjiayuan.pda_supermarket.ui.fragment.cashier;

import com.manfenjiayuan.pda_supermarket.database.entity.PosProductEntity;
import com.mfh.framework.mvp.MvpView;

/**
 * Created by bingshanguxue on 16/3/17.
 */
public interface ICashierView extends MvpView {
    /**
     * 查询到商品
     * */
    void onFindGoods(PosProductEntity goods, int packFlag);

    /**
     * 查询到生鲜商品
     * */
    void onFindFreshGoods(PosProductEntity goods, Double weight);


    /**
     * 未找到商品
     * */
    void onFindGoodsEmpty(String barcode);
}
