package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.view.IScProductView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.sc.product.ScProduct;
import com.mfh.framework.api.anon.sc.product.ScProductMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ScProductPresenter {
    private IScProductView mIScProductView;
    private ScProductMode mScProductMode;

    public ScProductPresenter(IScProductView iScProductView) {
        this.mIScProductView = iScProductView;
        this.mScProductMode = new ScProductMode();
    }

    /**
     * 获取批发商商品
     * @param companyId 批发商编号
     * */
    public void findProductByFrontCatalog(PageInfo pageInfo, Long frontCataLogId){
        mScProductMode.findProductByFrontCatalog(pageInfo, frontCataLogId,
                new OnPageModeListener<ScProduct>() {
            @Override
            public void onProcess() {
                if (mIScProductView != null) {
                    mIScProductView.onIScProductViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ScProduct> dataList) {
                if (mIScProductView != null) {
                    mIScProductView.onIScProductViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScProductView != null) {
                    mIScProductView.onIScProductViewError(errorMsg);
                }
            }
        });
    }



}
