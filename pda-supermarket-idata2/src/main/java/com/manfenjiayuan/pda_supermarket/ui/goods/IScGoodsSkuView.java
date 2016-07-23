package com.manfenjiayuan.pda_supermarket.ui.goods;

import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/21.
 */
public interface IScGoodsSkuView extends MvpView {
    void onIScGoodsSkuViewProcess();
    void onIScGoodsSkuViewError(String errorMsg);
    void onIScGoodsSkuViewSuccess(List<ScGoodsSku> scGoodsSkus);
    void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku);
}
