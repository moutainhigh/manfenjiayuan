package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ScGoodsSkuMode {
    /**
     * 查询商品
     */
    public void findGoodsListByFrontCategory(Long categoryId, PageInfo pageInfo,
                                             final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (categoryId == null) {
            if (listener != null) {
                listener.onError("categoryId 无效");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        scGoodsSkus.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, scGoodsSkus);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findGoodsListByFrontCategory(categoryId, pageInfo, queryRsCallBack);
    }

    /**
     * 查询后台类目商品
     */
    public void findGoodsListByBackendCategory(Long procateId, PageInfo pageInfo, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (procateId == null) {
            if (listener != null) {
                listener.onError("类目编号无效");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        scGoodsSkus.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, scGoodsSkus);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findGoodsListByBackendCategory(procateId, pageInfo, queryRsCallBack);
    }

    /**
     * 查询网点商品档案
     */
    public void findGoodsListByBarcode(String barcode, PageInfo pageInfo, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                        //此处在主线程中执行。
                        List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                                scGoodsSkus.add(wrapper.getBean());
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, scGoodsSkus);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载类目商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findGoodsListByBarcode(barcode, pageInfo, queryRsCallBack);
    }

    /**
     * 查询商品
     */
    public void findGoodsListByName(String name, PageInfo pageInfo,
                                    final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(name)) {
            if (listener != null) {
                listener.onError("商品名称不能为空");
            }
            return;
        }


        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> scGoodsSkus = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        scGoodsSkus.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, scGoodsSkus);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.d("加载类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findGoodsListByName(name, pageInfo, queryRsCallBack);
    }

    /**
     * 查询商品
     */
    public void getGoodsByBarCode(String barcode, final OnModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        ScGoodsSku goodsSku = null;
                        if (rspData != null) {
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            goodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(goodsSku);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询失败: " + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , ScGoodsSku.class
                , MfhApplication.getAppContext()) {
        };

        ScGoodsSkuApiImpl.getGoodsByBarCode(barcode, queryResCallback);
    }

    /**
     * 查询商品
     */
    public void getByBarcode(String barcode, final OnModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        ScGoodsSku goodsSku = null;
                        if (rspData != null) {
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            goodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(goodsSku);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询失败: " + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , ScGoodsSku.class
                , MfhApplication.getAppContext()) {
        };

        ScGoodsSkuApiImpl.getByBarcode(barcode, queryResCallback);
    }

    /**
     * 获取库存商品
     *
     * @param categoryId 类目编号
     */
    public void listScGoodsSku(PageInfo pageInfo, Long categoryId, String barcode, String name,
                               String orderby, boolean orderbydesc,
                               String priceType, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.df("加载库存商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.listScGoodsSku(pageInfo, categoryId, barcode, name,
                orderby, orderbydesc, false, priceType, queryRsCallBack);
    }

    public void listScGoodsSku(Long categoryId, PageInfo pageInfo, final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
                //此处在主线程中执行。
                List<ScGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ScGoodsSku> wrapper : rs.getRowDatas()) {
                        entityList.add(wrapper.getBean());
                    }
                }
                if (listener != null) {
                    listener.onSuccess(pageInfo, entityList);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                ZLogger.df("加载库存商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.listScGoodsSku(categoryId, pageInfo, false, queryRsCallBack);
    }

    /**
     * 根据条码查找租户是否已经发布过该商品，若存在返回信息
     */
    public void checkWithBuyInfoByBarcode(String barcode, final OnModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }


        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
                NetProcessor.Processor<ScGoodsSku>>(
                new NetProcessor.Processor<ScGoodsSku>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //查询失败
                        ZLogger.df("查询商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        ScGoodsSku goods = null;

                        if (rspData != null) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                            goods = retValue.getValue();
                        }

                        if (listener != null) {
                            listener.onSuccess(goods);
                        }
                    }
                }
                , ScGoodsSku.class
                , MfhApplication.getAppContext()) {
        };
        ScGoodsSkuApiImpl.checkWithBuyInfoByBarcode(barcode, responseCallback);
    }
}
