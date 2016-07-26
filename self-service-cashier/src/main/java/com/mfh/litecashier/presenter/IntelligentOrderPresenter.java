package com.mfh.litecashier.presenter;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.mode.InventoryGoodsMode;
import com.mfh.litecashier.ui.view.IInventoryView;

import java.util.List;

/**
 * 智能订货
 * Created by bingshanguxue on 16/3/17.
 */
public class IntelligentOrderPresenter {
    private IInventoryView iInventoryView;
    private InventoryGoodsMode iInventoryGoodsMode;

    public IntelligentOrderPresenter(IInventoryView iInventoryView) {
        this.iInventoryView = iInventoryView;
        this.iInventoryGoodsMode = new InventoryGoodsMode();
    }

    /**
     * 加载库存商品
     */
    public void loadInventoryGoods(PageInfo pageInfo, String categoryId, String barcode, String name,
                           int sortType, String priceType) {

        iInventoryGoodsMode.loadInventoryGoods(pageInfo, categoryId, barcode, name,
                sortType, priceType, new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (iInventoryView != null){
                            iInventoryView.onProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (iInventoryView != null){
                            iInventoryView.onList(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (iInventoryView != null){
                            iInventoryView.onError(errorMsg);
                        }
                    }
                });
    }
}
