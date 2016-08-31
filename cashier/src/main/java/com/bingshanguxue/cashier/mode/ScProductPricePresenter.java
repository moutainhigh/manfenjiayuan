package com.bingshanguxue.cashier.mode;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.anon.PubSkus;
import com.mfh.framework.api.anon.ScProductPriceMode;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ScProductPricePresenter {
    private IScProductPriceView mIScProductPriceView;
    private ScProductPriceMode mScProductPriceMode;

    public ScProductPricePresenter(IScProductPriceView iScProductPriceView) {
        this.mIScProductPriceView = iScProductPriceView;
        this.mScProductPriceMode = new ScProductPriceMode();
    }

    /**
     * 获取批发商商品
     * @param frontCataLogId 前台类目编号
     * */
    public void findProductByFrontCatalog(PageInfo pageInfo, Long frontCataLogId){
        mScProductPriceMode.findProductByFrontCatalog(pageInfo, frontCataLogId,
                new OnPageModeListener<PubSkus>() {
            @Override
            public void onProcess() {
                if (mIScProductPriceView != null) {
                    mIScProductPriceView.onIScProductPriceViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<PubSkus> dataList) {
                if (mIScProductPriceView != null) {
                    mIScProductPriceView.onIScProductPriceViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScProductPriceView != null) {
                    mIScProductPriceView.onIScProductPriceViewError(errorMsg);
                }
            }
        });
    }



}
