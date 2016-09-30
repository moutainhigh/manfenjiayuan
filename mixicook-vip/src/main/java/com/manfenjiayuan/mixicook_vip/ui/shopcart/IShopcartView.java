package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.shoppingCart.ShoppingCart;
import com.mfh.framework.mvp.MvpView;

import java.util.List;

/**
 * 购物车
 * Created by bingshanguxue on 16/3/17.
 */
public interface IShopcartView extends MvpView {
    void onIShopcartViewProcess();

    void onIShopcartViewError(String errorMsg);

    void onIShopcartViewSuccess(PageInfo pageInfo, List<ShoppingCart> dataList);

}
