package com.manfenjiayuan.business.view;

import com.mfh.comn.bean.PageInfo;
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
    void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> scGoodsSkus);
    void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku);
}
