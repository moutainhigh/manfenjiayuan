package com.manfenjiayuan.mixicook_vip.ui.location;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.api.subdist.SubdisMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品采购
 * Created by bingshanguxue on 16/3/17.
 */
public class SubdisPresenter {
    private ISubdisView mSubdisView;
    private SubdisMode mSubdisMode;

    public SubdisPresenter(ISubdisView iSubdisView) {
        this.mSubdisView = iSubdisView;
        this.mSubdisMode = new SubdisMode();
    }

    /**
     * 加载采购商品
     * */
    public void findArroundSubdist(String longitude, String latitude, PageInfo pageInfo){
        mSubdisMode.findArroundSubdist(longitude, latitude, pageInfo,
                new OnPageModeListener<Subdis>() {
                    @Override
                    public void onProcess() {
                        if (mSubdisView != null) {
                            mSubdisView.onISubdisViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<Subdis> dataList) {
                        if (mSubdisView != null) {
                            mSubdisView.onISubdisViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mSubdisView != null) {
                            mSubdisView.onISubdisViewError(errorMsg);
                        }
                    }
                });
    }

}
