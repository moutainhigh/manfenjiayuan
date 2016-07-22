package com.manfenjiayuan.pda_supermarket.ui.goods;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class GoodsPresenter {
    private IGoodsView mIGoodsView;
    private ScGoodsSkuMode mScGoodsSkuMode;

    public GoodsPresenter(IGoodsView mIGoodsView) {
        this.mIGoodsView = mIGoodsView;
        this.mScGoodsSkuMode = new ScGoodsSkuMode();
    }

    public void findGoodsList(String barcode) {
        mScGoodsSkuMode.findGoodsList(barcode, new PageInfo(-1, 10),
                new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIGoodsView != null) {
                            mIGoodsView.onIGoodsViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIGoodsView != null) {
                            mIGoodsView.onIGoodsViewSuccess(dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIGoodsView != null) {
                            mIGoodsView.onIGoodsViewError(errorMsg);
                        }
                    }
                });
    }


}
