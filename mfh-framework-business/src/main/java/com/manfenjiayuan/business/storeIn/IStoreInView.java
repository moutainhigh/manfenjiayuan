package com.manfenjiayuan.business.storeIn;

import com.mfh.framework.mvp.MvpView;

/**
 * 商品
 * Created by bingshanguxue on 16/3/21.
 */
public interface IStoreInView extends MvpView {
    void onIStoreInViewProcess();
    void onIStoreInViewError(String errorMsg);
    void onIStoreInViewSuccess();
}
