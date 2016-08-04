package com.manfenjiayuan.business.view;

import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.mfh.framework.mvp.MvpView;

/**
 * Created by bingshanguxue on 16/3/21.
 */
public interface IInvSkuGoodsView extends MvpView {
    void onIInvSkuGoodsViewProcess();
    void onIInvSkuGoodsViewError(String errorMsg);
    void onIInvSkuGoodsViewSuccess(InvSkuGoods data);
}
