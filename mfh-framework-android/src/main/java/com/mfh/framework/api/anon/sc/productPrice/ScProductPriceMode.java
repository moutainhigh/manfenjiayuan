package com.mfh.framework.api.anon.sc.productPrice;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.AnonScHttpManager;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScProductPriceMode {
    /**
     * 查询前台类目商品
     * */
    public void findProductByFrontCatalog(PageInfo pageInfo, Long frontCataLogId,
                                          final OnPageModeListener<PubSkus> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (RxHttpManager.RELEASE){
            Map<String, String> options = new HashMap<>();
            if (frontCataLogId != null) {
                options.put("frontCataLogId", String.valueOf(frontCataLogId));
            }
            options.put("priceMask", "0");
            options.put("includeSub", "true");

            if (pageInfo != null){
                options.put("page", Integer.toString(pageInfo.getPageNo()));
                options.put("rows", Integer.toString(pageInfo.getPageSize()));
            }
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            AnonScHttpManager.getInstance().findPubSkusByFrontCatalog(options,
                    new MQuerySubscriber<PubSkus>(pageInfo) {
                        @Override
                        public void onQueryNext(PageInfo pageInfo, List<PubSkus> dataList) {
                            super.onQueryNext(pageInfo, dataList);
                            if (listener != null) {
                                listener.onSuccess(pageInfo, dataList);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            super.onError(e);

                            ZLogger.df("加载前台类目商品失败:" + e.toString());
                            if (listener != null) {
                                listener.onError(e.toString());
                            }
                        }
                    });
        }
        else{
            NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                    new NetProcessor.QueryRsProcessor<PubSkus>(pageInfo) {
                        @Override
                        public void processQueryResult(RspQueryResult<PubSkus> rs) {
                            //此处在主线程中执行。
                            List<PubSkus> entityList = new ArrayList<>();
                            if (rs != null) {
                                for (EntityWrapper<PubSkus> wrapper : rs.getRowDatas()) {
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
                            ZLogger.d("加载前台类目商品失败:" + errMsg);
                            if (listener != null) {
                                listener.onError(errMsg);
                            }
                        }
                    }, PubSkus.class, MfhApplication.getAppContext());

            ScProductPriceApi.findProductByFrontCatalog(frontCataLogId, pageInfo, queryRsCallBack);
        }
    }

    /**
     * 查询平台商品档案
     * */
    public void findProductSku(String barcode, PageInfo pageInfo,
                               final OnPageModeListener<ProductSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ProductSku>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ProductSku> rs) {
                        //此处在主线程中执行。
                        List<ProductSku> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ProductSku> wrapper : rs.getRowDatas()) {
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
                        ZLogger.d("加载前台类目商品失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, ProductSku.class, MfhApplication.getAppContext());

        ScProductPriceApi.findProductSku(barcode, queryRsCallBack);
    }
}
