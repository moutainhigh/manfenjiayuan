package com.mfh.framework.api.scChainGoodsSku;

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
import com.mfh.framework.rxapi.httpmgr.ScChainGoodsSkuHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存商品：库存成本，批次流水，库存调拨
 * Created by bingshanguxue on 16/3/17.
 */
public class ChainGoodsSkuMode {



    public void findPublicChainGoodsSku2(PageInfo pageInfo, Long frontCategoryId, Long companyId,
                                         String barcode,
                                         final OnPageModeListener<ChainGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (frontCategoryId == null) {
            if (listener != null) {
                listener.onError("缺少必要参数frontCategoryId");
            }
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<ChainGoodsSku>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<ChainGoodsSku> rs) {
                //此处在主线程中执行。
                List<ChainGoodsSku> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<ChainGoodsSku> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载洗衣类目商品失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, ChainGoodsSku.class, MfhApplication.getAppContext());

        ScChainGoodsSkuApiImpl.findPublicChainGoodsSku2(frontCategoryId, companyId, barcode, pageInfo,
                queryRsCallBack);
    }

    public void findSupplyChainGoodsSku(String barcode, Long proSkuId, String nameLike,
                                        PageInfo pageInfo,
                                        final OnPageModeListener<ChainGoodsSku> listener) {

        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        if (!StringUtils.isEmpty(barcode)) {
            options.put("barcode", barcode);
        }
        if (proSkuId != null) {
            options.put("proSkuId", String.valueOf(proSkuId));
        }
        if (!StringUtils.isEmpty(nameLike)) {
            options.put("nameLike", nameLike);
        }
        if (pageInfo != null) {
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ScChainGoodsSkuHttpManager.getInstance().findSupplyChainGoodsSku(options,
                new MQuerySubscriber<ChainGoodsSku>(pageInfo) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.d("加载洗衣类目商品失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }
                });

    }


    /**
     * 查询供应商商品
     */
    public void findTenantSku(PageInfo pageInfo, Long companyId, Long frontCategoryId,
                              String barcode,
                              final OnPageModeListener<ChainGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ChainGoodsSku>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ChainGoodsSku> rs) {
                        //此处在主线程中执行。
                        List<ChainGoodsSku> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ChainGoodsSku> wrapper : rs.getRowDatas()) {
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
                        ZLogger.d("加载采购商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ChainGoodsSku.class, MfhApplication.getAppContext());

        ScChainGoodsSkuApiImpl.findTenantSku(barcode, companyId, frontCategoryId,
                pageInfo, queryRsCallBack);
    }

    public void getTenantSkuMust(Long tenantId, String barcode, boolean needLike,
                                 final OnModeListener<ChainGoodsSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<ChainGoodsSku,
                NetProcessor.Processor<ChainGoodsSku>>(
                new NetProcessor.Processor<ChainGoodsSku>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //查询失败
                        ZLogger.d("加载商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"新增成功!","version":"1","data":{"val":"463"}}
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                        ChainGoodsSku chainGoodsSku = null;
                        if (rspData != null) {
                            RspBean<ChainGoodsSku> retValue = (RspBean<ChainGoodsSku>) rspData;
                            chainGoodsSku = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(chainGoodsSku);
                        }
                    }
                }
                , ChainGoodsSku.class
                , MfhApplication.getAppContext()) {
        };

        ScChainGoodsSkuApiImpl.getTenantSkuMust(barcode, tenantId, needLike, responseCallback);
    }
}
