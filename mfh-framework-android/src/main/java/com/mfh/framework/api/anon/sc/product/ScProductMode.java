package com.mfh.framework.api.anon.sc.product;

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
public class ScProductMode {
    /**
     * 查询供应商商品
     * */
    public void findProductByFrontCatalog(PageInfo pageInfo, Long frontCataLogId,
                                          final OnPageModeListener<ScProduct> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ScProduct>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<ScProduct> rs) {
                        //此处在主线程中执行。
                        List<ScProduct> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<ScProduct> wrapper : rs.getRowDatas()) {
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
                }, ScProduct.class, MfhApplication.getAppContext());

        ScProductApi.findProductByFrontCatalog(frontCataLogId,
                pageInfo, queryRsCallBack);
    }
}
