package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuMode;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class ScGoodsSkuPresenter {
    private IScGoodsSkuView mIScGoodsSkuView;
    private ScGoodsSkuMode mScGoodsSkuMode;

    public ScGoodsSkuPresenter(IScGoodsSkuView mIScGoodsSkuView) {
        this.mIScGoodsSkuView = mIScGoodsSkuView;
        this.mScGoodsSkuMode = new ScGoodsSkuMode();
    }

    public void findGoodsListByFrontCategory(Long catogoryId, PageInfo pageInfo) {
        mScGoodsSkuMode.findGoodsListByFrontCategory(catogoryId, pageInfo,
                new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }

    public void findGoodsListByBackendCategory(Long procateId, PageInfo pageInfo) {
        mScGoodsSkuMode.findGoodsListByBackendCategory(procateId, pageInfo,
                new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }


    public void findGoodsListByBarcode(String barcode) {
        mScGoodsSkuMode.findGoodsListByBarcode(barcode, new PageInfo(-1, 10),
                new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }

    public void findGoodsListByName(String name) {
        mScGoodsSkuMode.findGoodsListByName(name, new PageInfo(-1, 10),
                new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null) {
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }


    public void getGoodsByBarCode(String barcode) {
        mScGoodsSkuMode.getGoodsByBarCode(barcode, new OnModeListener<ScGoodsSku>() {
            @Override
            public void onProcess() {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(ScGoodsSku data) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                }
            }

        });
    }

    public void getByBarcode(String barcode) {
        mScGoodsSkuMode.getByBarcode(barcode, new OnModeListener<ScGoodsSku>() {
            @Override
            public void onProcess() {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(ScGoodsSku data) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (mIScGoodsSkuView != null) {
                    mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                }
            }

        });
    }

    /**
     * 加载库存商品
     */
    public void listScGoodsSku(PageInfo pageInfo, Long categoryId, String barcode, String name,
                               String orderby, boolean orderbydesc, String priceType) {

        mScGoodsSkuMode.listScGoodsSku(pageInfo, categoryId, barcode, name,
                orderby, orderbydesc, priceType, new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }

    public void listScGoodsSku(Long categoryId, PageInfo pageInfo) {

        mScGoodsSkuMode.listScGoodsSku(categoryId, pageInfo, new OnPageModeListener<ScGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }

}
