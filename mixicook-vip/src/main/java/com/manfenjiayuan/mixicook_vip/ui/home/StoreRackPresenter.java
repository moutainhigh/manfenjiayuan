package com.manfenjiayuan.mixicook_vip.ui.home;

import com.mfh.framework.api.anon.sc.storeRack.ScStoreRackMode;
import com.mfh.framework.api.anon.sc.storeRack.StoreRack;
import com.mfh.framework.mvp.OnModeListener;

/**
 * 货架
 * Created by bingshanguxue on 16/3/17.
 */
public class StoreRackPresenter {
    private IStoreRackView mIReciaddrView;
    private ScStoreRackMode mScStoreRackMode;

    public StoreRackPresenter(IStoreRackView iStoreRackView) {
        this.mIReciaddrView = iStoreRackView;
        this.mScStoreRackMode = new ScStoreRackMode();
    }

    /**
     * 查询货架商品
     * */
    public void getById(Long rackId){
        mScStoreRackMode.getById(rackId,
                new OnModeListener<StoreRack>() {
                    @Override
                    public void onProcess() {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIStoreRackViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(StoreRack data) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIStoreRackViewSuccess(data);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIReciaddrView != null) {
                            mIReciaddrView.onIStoreRackViewError(errorMsg);
                        }
                    }
                });
    }

}
