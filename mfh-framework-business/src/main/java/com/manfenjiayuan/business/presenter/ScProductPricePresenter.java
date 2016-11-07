package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IScProcuctPriceView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.sc.productPrice.ProductSku;
import com.mfh.framework.api.anon.sc.productPrice.ScProductPriceMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 平台商品档案
 * Created by bingshanguxue on 16/3/17.
 */
public class ScProductPricePresenter {
    private IScProcuctPriceView mIScProcuctPriceView;
    private ScProductPriceMode mScProductPriceMode;

    public ScProductPricePresenter(IScProcuctPriceView iScProcuctPriceView) {
        this.mIScProcuctPriceView = iScProcuctPriceView;
        this.mScProductPriceMode = new ScProductPriceMode();
    }

    /**
     * 查询平台商品档案
     * @param barcode 商品条码
     * */
    public void findProductSku(String barcode, PageInfo pageInfo){
        mScProductPriceMode.findProductSku(barcode, pageInfo,
                new OnPageModeListener<ProductSku>() {
            @Override
            public void onProcess() {
                if (mIScProcuctPriceView != null) {
                    mIScProcuctPriceView.onScProcuctPriceViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ProductSku> dataList) {
                if (mIScProcuctPriceView != null) {
                    mIScProcuctPriceView.onScProcuctPriceViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScProcuctPriceView != null) {
                    mIScProcuctPriceView.onScProcuctPriceViewError(errorMsg);
                }
            }
        });
    }

}
