package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuMode;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.httpmgr.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商超库存商品
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
    public void listScGoodsSku(PageInfo pageInfo, String categoryId, String barcode, String name,
                               String orderby, boolean orderbydesc, String priceType) {

        if (mIScGoodsSkuView != null){
            mIScGoodsSkuView.onIScGoodsSkuViewProcess();
        }

        Map<String, String> options = new HashMap<>();
        //类目
        if (!StringUtils.isEmpty(categoryId)) {
            options.put("categoryId", categoryId);
        }
//        价格类型0-计件 1-计重
        if (!StringUtils.isEmpty(priceType)) {
            options.put("priceType", priceType);
        }
        //排序
        if (!StringUtils.isEmpty(orderby)) {
            options.put("orderby", orderby);
            options.put("orderbydesc", String.valueOf(orderbydesc));
        }
        //gku.sell_day_num
        options.put("joinFlag", String.valueOf(false));// 只查网点商品
        if (!StringUtils.isEmpty(barcode)) {
            options.put("barcode", barcode);
        }
        if (!StringUtils.isEmpty(name)) {
            options.put("name", name);
        }

        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ScGoodsSkuHttpManager.getInstance().list(options,
                new MQuerySubscriber<ScGoodsSku>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.e("加载库存商品失败:" + e.toString());

                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewError(e.getMessage());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (mIScGoodsSkuView != null){
                            mIScGoodsSkuView.onIScGoodsSkuViewSuccess(pageInfo, dataList);
                        }
                    }
                });

    }



    public void findOnlineGoodsList(Long netId, String proSkuIds, PageInfo pageInfo) {
        mScGoodsSkuMode.findOnlineGoodsList(netId, proSkuIds, pageInfo,
                new OnPageModeListener<ScGoodsSku>() {
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
    public void findOnlineGoodsList2(Long netId, Long frontCategoryId, PageInfo pageInfo) {
        mScGoodsSkuMode.findOnlineGoodsList2(netId, frontCategoryId, pageInfo,
                new OnPageModeListener<ScGoodsSku>() {
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
