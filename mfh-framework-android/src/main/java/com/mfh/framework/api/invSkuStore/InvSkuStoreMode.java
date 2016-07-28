package com.mfh.framework.api.invSkuStore;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrder;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApi;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItemBrief;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.AfinalFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import net.tsz.afinal.http.AjaxParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 7/28/16.
 */
public class InvSkuStoreMode {
    /**
     * 加载库存调拨订单列表
     * @param pageInfo
     * @param status 订单状态，可以为空，为空时表示查询所有状态
     * @param payStatus 支付状态，可以为空，为空时表示查询所有状态。
     * @param sendTenantId 发货方网点编号，可以为空，为空时表示查询所有发货发。
     * @param listener
     * */
    public void autoAskSendOrder(Long chainCompanyId,
                                final OnModeListener<InvSendOrderItemBrief> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<InvSendOrderItemBrief,
                NetProcessor.Processor<InvSendOrderItemBrief>>(
                new NetProcessor.Processor<InvSendOrderItemBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {

                        InvSendOrderItemBrief invSendOrderItemBrief = null;

                        if (rspData != null) {
                            //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<InvSendOrderItemBrief> retValue = (RspBean<InvSendOrderItemBrief>) rspData;
                            invSendOrderItemBrief = retValue.getValue();
                        }

                        if (listener != null) {
                            listener.onSuccess(invSendOrderItemBrief);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("智能订货失败：" + errMsg);

                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , InvSendOrderItemBrief.class
                , MfhApplication.getAppContext()) {
        };

        InvSkuStoreApiImpl.autoAskSendOrder(chainCompanyId, responseCallback);
    }
}
