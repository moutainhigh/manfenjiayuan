package com.manfenjiayuan.mixicook_vip.ui.home;

import com.mfh.framework.api.anon.sc.storeRack.StoreRack;
import com.mfh.framework.mvp.MvpView;

/**
 * 货架
 * Created by bingshanguxue on 16/3/17.
 */
public interface IStoreRackView extends MvpView {
    void onIStoreRackViewProcess();
    void onIStoreRackViewError(String errorMsg);
    void onIStoreRackViewSuccess(StoreRack data);
}
