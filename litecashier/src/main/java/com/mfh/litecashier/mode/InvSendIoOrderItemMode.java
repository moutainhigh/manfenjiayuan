package com.mfh.litecashier.mode;

import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.litecashier.CashierApp;

import java.util.ArrayList;
import java.util.List;

/**
 * 采购收货订单明细
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendIoOrderItemMode implements IInvSendIoOrderItemMode<InvSendIoOrderItem> {

    @Override
    public void loadOrderItems(Long id, final OnPageModeListener<InvSendIoOrderItem> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (id == null){
            if (listener != null) {
                listener.onError("缺少id参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<InvSendIoOrderItemBrief,
                NetProcessor.Processor<InvSendIoOrderItemBrief>>(
                new NetProcessor.Processor<InvSendIoOrderItemBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {

                        List<InvSendIoOrderItem> entityList = new ArrayList<>();
                        if (rspData != null) {
                            //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                            RspBean<InvSendIoOrderItemBrief> retValue = (RspBean<InvSendIoOrderItemBrief>) rspData;
                            InvSendIoOrderItemBrief orderDetail = retValue.getValue();
                            entityList = orderDetail.getItems();
                        }

                        if (listener != null) {
                            listener.onSuccess(null, entityList);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载采购收货订单明细失败：" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , InvSendIoOrderItemBrief.class
                , CashierApp.getAppContext()) {
        };

        InvOrderApiImpl.getInvSendIoOrderById(id, responseCallback);
    }
}
