package com.manfenjiayuan.pda_supermarket.ui.goods;

import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSkuMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ChainGoodsSkuPresenter {
    private IChainGoodsSkuView iChainGoodsSkuView;
    private ChainGoodsSkuMode iChainGoodsSkuMode;

    public ChainGoodsSkuPresenter(IChainGoodsSkuView iChainGoodsSkuView) {
        this.iChainGoodsSkuView = iChainGoodsSkuView;
        this.iChainGoodsSkuMode = new ChainGoodsSkuMode();
    }

    /**
     * 查询批发商商品
     * @param frontCategoryId 类目编号
     * */
    public void findSupplyChainGoodsSku(String barcode, Long proSkuId, String nameLike){
        iChainGoodsSkuMode.findSupplyChainGoodsSku(barcode, proSkuId, nameLike,
                new OnPageModeListener<ChainGoodsSku>() {
            @Override
            public void onProcess() {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onError(errorMsg);
                }
            }
        });
    }

}
