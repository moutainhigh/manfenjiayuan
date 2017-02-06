package com.mfh.framework.api.invLossOrder;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bingshanguxue on 19/11/2016.
 */

public class InvLossOrderMode {

    /**
     * 盘点列表
     */
    public void list(PageInfo pageInfo, final OnPageModeListener<InvLossOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<InvLossOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<InvLossOrder> rs) {
                        List<InvLossOrder> entityList = new ArrayList<>();
                        if (rs != null) {
                            for (EntityWrapper<InvLossOrder> wrapper : rs.getRowDatas()) {
                                InvLossOrder invLossOrder = wrapper.getBean();
                                Map<String, String> caption = wrapper.getCaption();
                                if (caption != null) {
                                    invLossOrder.setStatusCaption(caption.get("status"));
                                }
                                entityList.add(invLossOrder);
                            }
                        }
                        if (listener != null) {
                            listener.onSuccess(pageInfo, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载报损订单失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }, InvLossOrder.class, MfhApplication.getAppContext());

        InvOrderApiImpl.queryInvLossOrderList(pageInfo, queryRsCallBack);
    }
}
