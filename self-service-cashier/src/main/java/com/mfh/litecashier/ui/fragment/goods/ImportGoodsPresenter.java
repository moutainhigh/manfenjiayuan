package com.mfh.litecashier.ui.fragment.goods;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.InvSkuStoreHttpManager;
import com.mfh.framework.rxapi.http.ProductCatalogManager;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by bingshanguxue on 01/04/2017.
 */

public class ImportGoodsPresenter {
    private IImportGoodsView mIImportGoodsView;

    public ImportGoodsPresenter(IImportGoodsView IImportGoodsView) {
        mIImportGoodsView = IImportGoodsView;
    }

    /**
     * 建档
     */
    public void importFromCenterSkus(final Long categoryId, final String productIds, String proSkuIds) {
        if (mIImportGoodsView != null) {
            mIImportGoodsView.onIImportGoodsViewProcess();
        }

        Map<String, String> options = new HashMap<>();
        options.put("proSkuIds", proSkuIds);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSkuStoreHttpManager.getInstance().importFromCenterSkus(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("导入商品到本店仓储失败, " + e.toString());
                if (mIImportGoodsView != null) {
                    mIImportGoodsView.onIImportGoodsViewError(e.getMessage());
                }
            }

            @Override
            public void onNext(String s) {
                ZLogger.df("导入商品到本店仓储成功");
                add2Category(categoryId, productIds);
            }

        });
    }

    /**
     * 导入类目
     */
    private void add2Category(Long categoryId, String productIds) {
        Map<String, String> options = new HashMap<>();
        options.put("groupIds", String.valueOf(categoryId));
        options.put("productIds", productIds);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ProductCatalogManager.getInstance().addToCatalog(options, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.df("导入前台类目商品失败, " + e.toString());
                if (mIImportGoodsView != null) {
                    mIImportGoodsView.onIImportGoodsViewError(e.getMessage());
                }
            }

            @Override
            public void onNext(String s) {
                ZLogger.df("导入前台类目商品成功");
                if (mIImportGoodsView != null) {
                    mIImportGoodsView.onIImportGoodsViewSuccess();
                }
            }

        });
    }
}
