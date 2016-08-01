package com.manfenjiayuan.pda_supermarket.ui.goods;

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


    public void findGoodsList(String barcode) {
        mScGoodsSkuMode.findGoodsList(barcode, new PageInfo(-1, 10),
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
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(dataList);
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
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(dataList);
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

}
