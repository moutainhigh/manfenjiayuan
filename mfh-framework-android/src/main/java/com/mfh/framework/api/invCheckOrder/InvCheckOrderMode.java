package com.mfh.framework.api.invCheckOrder;

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
import java.util.Map;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class InvCheckOrderMode {

    /**
     * 盘点列表
     */
    public void list(PageInfo pageInfo, final OnPageModeListener<InvCheckOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<InvCheckOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<InvCheckOrder> rs) {
                        //此处在主线程中执行。
                        List<InvCheckOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<InvCheckOrder> wrapper : rs.getRowDatas()) {
                                InvCheckOrder invCheckOrder = wrapper.getBean();
                                Map<String, String> caption = wrapper.getCaption();
                                if (caption != null) {
                                    invCheckOrder.setStatusCaption(caption.get("status"));
                                    invCheckOrder.setStoreTypeCaption(caption.get("storeType"));
                                    invCheckOrder.setNetCaption(caption.get("netId"));
                                }
                                entityList.add(invCheckOrder);
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载待拣货订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, InvCheckOrder.class, MfhApplication.getAppContext());

        InvCheckOrderApiImpl.list(pageInfo, queryRsCallBack);
    }
}
