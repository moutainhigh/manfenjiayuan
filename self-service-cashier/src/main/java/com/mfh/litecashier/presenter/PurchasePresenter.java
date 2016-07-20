package com.mfh.litecashier.presenter;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.litecashier.bean.FruitScGoodsSku;
import com.mfh.litecashier.mode.PurchaseMode;
import com.mfh.litecashier.ui.view.IPurchaseView;

import java.util.List;

/**
 * 商品采购
 * Created by bingshanguxue on 16/3/17.
 */
public class PurchasePresenter {
    private IPurchaseView mIPurchaseView;
    private PurchaseMode mPurchaseMode;

    public PurchasePresenter(IPurchaseView mIPurchaseView) {
        this.mIPurchaseView = mIPurchaseView;
        this.mPurchaseMode = new PurchaseMode();
    }

    /**
     * 加载采购商品
     * */
    public void loadPurchaseGoods(PageInfo pageInfo, String categoryId, Long otherTenantId,
                                  String barcode, String nameLike, int sortType, String priceType){
        mPurchaseMode.loadPurchaseGoods(pageInfo, categoryId, otherTenantId, barcode,
                nameLike, sortType, priceType, new OnPageModeListener<FruitScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIPurchaseView != null) {
                            mIPurchaseView.onLoadPurchaseGoodsProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<FruitScGoodsSku> dataList) {
                        if (mIPurchaseView != null) {
                            mIPurchaseView.onLoadPurchaseGoodsFinished(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIPurchaseView != null) {
                            mIPurchaseView.onLoadPurchaseGoodsError(errorMsg);
                        }
                    }
                });
    }

}
