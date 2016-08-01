package com.manfenjiayuan.business.presenter;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSkuMode;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ChainGoodsSkuPresenter {
    private IChainGoodsSkuView iChainGoodsSkuView;
    private ChainGoodsSkuMode iChainGoodsSkuMode;

    public ChainGoodsSkuPresenter(IChainGoodsSkuView iChainGoodsSkuView) {
        this.iChainGoodsSkuView = iChainGoodsSkuView;
        this.iChainGoodsSkuMode = new ChainGoodsSkuMode();
    }

    /**
     * 查询洗衣类目商品商品
     * @param frontCategoryId 类目编号
     * */
    public void loadLaundryGoods(PageInfo pageInfo, Long frontCategoryId, Long netId){
        iChainGoodsSkuMode.findPublicChainGoodsSku(pageInfo, frontCategoryId, netId, new OnPageModeListener<ChainGoodsSku>() {
            @Override
            public void onProcess() {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 查询批发商商品
     * @param frontCategoryId 类目编号
     * */
    public void loadCompanyChainSkuGoods(PageInfo pageInfo, Long frontCategoryId,
                                         Long companyId, String barcode){
        iChainGoodsSkuMode.findPublicChainGoodsSku2(pageInfo, frontCategoryId, companyId, barcode,
                new OnPageModeListener<ChainGoodsSku>() {
            @Override
            public void onProcess() {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 查询批发商商品
     * @param barcode 商品条码
     * */
    public void findSupplyChainGoodsSku(String barcode, Long proSkuId, String nameLike){
        iChainGoodsSkuMode.findSupplyChainGoodsSku(barcode, proSkuId, nameLike,
                new OnPageModeListener<ChainGoodsSku>() {
                    @Override
                    public void onProcess() {
                        if (iChainGoodsSkuView != null) {
                            iChainGoodsSkuView.onChainGoodsSkuViewProcess();
                        }
                    }

                    @Override
                    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                        if (iChainGoodsSkuView != null) {
                            iChainGoodsSkuView.onChainGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (iChainGoodsSkuView != null) {
                            iChainGoodsSkuView.onChainGoodsSkuViewError(errorMsg);
                        }
                    }
                });
    }


    /**
     * 获取批发商商品
     * @param companyId 批发商编号
     * */
    public void findTenantSku(PageInfo pageInfo, Long companyId, String barcode){
        iChainGoodsSkuMode.findTenantSku(pageInfo, companyId, barcode, new OnPageModeListener<ChainGoodsSku>() {
            @Override
            public void onProcess() {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewSuccess(pageInfo, dataList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewError(errorMsg);
                }
            }
        });
    }

    /**
     * 适用场景：门店/批发商 收货查询商品
     * */
    public void getTenantSkuMust(Long tenantId, String barcode){
        iChainGoodsSkuMode.getTenantSkuMust(tenantId, barcode, new OnModeListener<ChainGoodsSku>() {
            @Override
            public void onProcess() {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewProcess();
                }
            }

            @Override
            public void onSuccess(ChainGoodsSku data) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewSuccess(data);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (iChainGoodsSkuView != null) {
                    iChainGoodsSkuView.onChainGoodsSkuViewError(errorMsg);
                }
            }
        });
    }

}
