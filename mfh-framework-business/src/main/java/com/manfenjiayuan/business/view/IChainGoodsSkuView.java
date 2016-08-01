package com.manfenjiayuan.business.view;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IChainGoodsSkuView extends MvpView {
    void onChainGoodsSkuViewProcess();
    void onChainGoodsSkuViewError(String errorMsg);
    void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList);
    void onChainGoodsSkuViewSuccess(ChainGoodsSku data);
}
