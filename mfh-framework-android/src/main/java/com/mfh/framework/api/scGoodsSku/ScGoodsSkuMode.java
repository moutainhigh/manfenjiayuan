package com.mfh.framework.api.scGoodsSku;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.ScGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

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
                        ZLogger.ef("查询失败: " + errMsg);
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

        Map<String, String> options = new HashMap<>();
        options.put("barcode", barcode);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ScGoodsSkuHttpManager.getInstance().getByBarcode(options, new Subscriber<ScGoodsSku>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                ZLogger.ef("查询失败: " + e.toString());
                if (listener != null) {
                    listener.onError(e.toString());
                }
            }

            @Override
            public void onNext(ScGoodsSku scGoodsSku) {
                if (listener != null) {
                    listener.onSuccess(scGoodsSku);
                }
            }

        });
    }

    /**
     * 获取库存商品
     *
     * @param categoryId 类目编号
     */
    public void listScGoodsSku(Long categoryId, PageInfo pageInfo,
                               final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        //类目
        if (categoryId != null) {
            options.put("categoryId", String.valueOf(categoryId));
        }
        //gku.sell_day_num
        options.put("joinFlag", String.valueOf(false));// 只查网点商品
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
                        ZLogger.ef("加载库存商品失败:" + e.toString());

                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ScGoodsSku> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }
                });
    }

    /**
     * 批量查询店铺商品信息
     */
    public void findOnlineGoodsList(Long netId, String proSkuIds, PageInfo pageInfo,
                                    final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
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
                ZLogger.ef("加载库存商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findOnlineGoodsList(netId, proSkuIds, pageInfo, queryRsCallBack);
    }
    /**
     * 批量查询店铺商品信息
     */
    public void findOnlineGoodsList2(Long netId, Long frontCategoryId, PageInfo pageInfo,
                                    final OnPageModeListener<ScGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScGoodsSku>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScGoodsSku> rs) {
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
                        ZLogger.ef("加载库存商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ScGoodsSku.class, MfhApplication.getAppContext());

        ScGoodsSkuApiImpl.findOnlineGoodsList2(netId, frontCategoryId, pageInfo, queryRsCallBack);
    }
}
