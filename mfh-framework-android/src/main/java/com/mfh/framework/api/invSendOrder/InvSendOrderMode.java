package com.mfh.framework.api.invSendOrder;

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
 * 采购订单
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendOrderMode {

    public void loadOrders(PageInfo pageInfo, boolean netFlag, Long netId, String sendTenantId,
                           String status, final OnPageModeListener<InvSendOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvSendOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvSendOrder> rs) {
                //此处在主线程中执行。
                List<InvSendOrder> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvSendOrder> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载采购订单失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, InvSendOrder.class, MfhApplication.getAppContext());

        InvSendOrderApiImpl.list(netId, netFlag, status, pageInfo,queryRsCallBack);
    }

    /**
     * 加载门店采购订单&生鲜预定订单
     * */
    public void listInvSendOrders2(PageInfo pageInfo, Long receiveNetId, String sendType,
                                   String status, String receiveMobile, final OnPageModeListener<InvSendOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvSendOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvSendOrder> rs) {
                //此处在主线程中执行。
                List<InvSendOrder> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvSendOrder> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载采购订单失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, InvSendOrder.class, MfhApplication.getAppContext());

        InvSendOrderApiImpl.listInvSendOrdes2(receiveNetId, sendType, status, receiveMobile, pageInfo,queryRsCallBack);
    }

    public void listInvSendOrders2(PageInfo pageInfo, Long receiveNetId, String sendType, String status, final OnPageModeListener<InvSendOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.QueryRsCallBack queryRsCallBack = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvSendOrder>(pageInfo) {
            @Override
            public void processQueryResult(RspQueryResult<InvSendOrder> rs) {
                //此处在主线程中执行。
                List<InvSendOrder> entityList = new ArrayList<>();
                if (rs != null) {
                    for (EntityWrapper<InvSendOrder> wrapper : rs.getRowDatas()) {
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
                ZLogger.d("加载采购订单失败:" + errMsg);
                if (listener != null) {
                    listener.onError(errMsg);
                }
            }
        }, InvSendOrder.class, MfhApplication.getAppContext());

        InvSendOrderApiImpl.listInvSendOrdes2(receiveNetId, sendType, status, pageInfo,queryRsCallBack);
    }

}
