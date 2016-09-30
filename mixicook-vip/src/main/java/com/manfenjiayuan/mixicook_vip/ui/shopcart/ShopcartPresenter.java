package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.shoppingCart.ShoppingCart;
import com.mfh.framework.api.shoppingCart.ShoppingCartMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 购物车
 * Created by bingshanguxue on 16/3/17.
 */
public class ShopcartPresenter {
    private IShopcartView mShopcartViews;
    private ShoppingCartMode mShoppingCartMode;

    public ShopcartPresenter(IShopcartView iShopcartView) {
        this.mShopcartViews = iShopcartView;
        this.mShoppingCartMode = new ShoppingCartMode();
    }

    /**
     * 加载采购商品
     * */
    public void list(Long shopId, PageInfo pageInfo){
        mShoppingCartMode.list(shopId, pageInfo,
                new OnPageModeListener<ShoppingCart>() {
                    @Override
                    public void onProcess() {
                        if (mShopcartViews != null) {
                            mShopcartViews.onIShopcartViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ShoppingCart> dataList) {
                        if (mShopcartViews != null) {
                            mShopcartViews.onIShopcartViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mShopcartViews != null) {
                            mShopcartViews.onIShopcartViewError(errorMsg);
                        }
                    }
                });
    }

}
