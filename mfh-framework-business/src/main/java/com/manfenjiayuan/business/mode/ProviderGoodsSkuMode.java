package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.InvSkuProvider;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.scChainGoodsSku.ScChainGoodsSkuApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 供应商商品
 * Created by bingshanguxue on 16/3/17.
 */
public class ProviderGoodsSkuMode {

    public void listInvSkuProvider(PageInfo pageInfo, Long providerId, String barcode,
                                  final OnPageModeListener<InvSkuProvider> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        //检查参数：
        if (providerId == null) {
            if (listener != null) {
                listener.onError("缺少必要参数providerId");
            }
            return;
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvSkuProvider>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvSkuProvider> rs) {
                //此处在主线程中执行。
                List<InvSkuProvider> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvSkuProvider> wrapper : rs.getRowDatas()) {
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
        }, InvSkuProvider.class, MfhApplication.getAppContext());

        ScChainGoodsSkuApiImpl.listInvSkuProvider(providerId, barcode, pageInfo, queryRsCallBack);
    }
}
