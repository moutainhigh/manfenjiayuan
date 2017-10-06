package com.mfh.framework.api.anon.sc.productPrice;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.httpmgr.AnonScHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScProductPriceMode {
    /**
     * 查询前台类目商品
     */
    public void findProductByFrontCatalog(PageInfo pageInfo, String frontCataLogId,
                                          final OnPageModeListener<PubSkus> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        Map<String, String> options = new HashMap<>();
        if (!StringUtils.isEmpty(frontCataLogId)) {
            options.put("frontCataLogId", frontCataLogId);
        }
        options.put("priceMask", "0");
        options.put("includeSub", "true");
        if (pageInfo != null) {
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

                        ZLogger.ef("加载前台类目商品失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }
                });
    }

    /**
     * 查询平台商品档案
     */
    public void findProductSku(Map<String, String> options, PageInfo pageInfo,
                               final OnPageModeListener<ProductSku> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        AnonScHttpManager.getInstance().findProductSku(options,
                new MQuerySubscriber<ProductSku>(pageInfo) {
                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ProductSku> dataList) {
                        super.onQueryNext(pageInfo, dataList);
                        if (listener != null) {
                            listener.onSuccess(pageInfo, dataList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        ZLogger.ef("加载前台类目商品失败:" + e.toString());
                        if (listener != null) {
                            listener.onError(e.toString());
                        }
                    }
                });
    }
}
