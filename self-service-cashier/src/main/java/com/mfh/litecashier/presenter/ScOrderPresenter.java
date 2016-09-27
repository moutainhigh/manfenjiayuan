package com.mfh.litecashier.presenter;

import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderMode;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.litecashier.ui.view.IScOrderView;

/**
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderPresenter {
    private IScOrderView mIScOrderView;
    private ScOrderMode mScOrderMode;

    public ScOrderPresenter(IScOrderView iScOrderView) {
        this.mIScOrderView = iScOrderView;
        this.mScOrderMode = new ScOrderMode();
    }

    /**
     * 查询订单
     * */
    public void getByCode(String barcode) {
        mScOrderMode.getByCode(barcode, new OnModeListener<ScOrder>() {
            @Override
            public void onProcess() {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewProcess();
                }
            }

            @Override
            public void onSuccess(ScOrder data) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewNext(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScOrderView != null){
                    mIScOrderView.onIScOrderViewError(errorMsg);
                }
            }
        });
    }
}
