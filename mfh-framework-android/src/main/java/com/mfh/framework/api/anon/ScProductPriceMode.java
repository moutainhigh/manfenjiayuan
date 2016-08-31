package com.mfh.framework.api.anon;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

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
