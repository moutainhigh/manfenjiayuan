package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItemBrief;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.InvSendOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * 采购订单明细
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendOrderItemMode implements IInvSendOrderItemMode<InvSendOrderItem> {

    @Override
    public void loadOrderItems(Long id, final OnPageModeListener<InvSendOrderItem> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (id == null){
            if (listener != null) {
                listener.onError("缺少id参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<InvSendOrderItemBrief,
                NetProcessor.Processor<InvSendOrderItemBrief>>(
                new NetProcessor.Processor<InvSendOrderItemBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {

                        if (rspData == null) {
                            if (listener != null) {
                                 listener.onSuccess(null, null);
                            }
                            return;
                        }
                        //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<InvSendOrderItemBrief> retValue = (RspBean<InvSendOrderItemBrief>) rspData;
                        InvSendOrderItemBrief orderDetail = retValue.getValue();

                        if (listener != null) {
                            listener.onSuccess(null, orderDetail.getItems());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载采购订单明细失败：" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , InvSendOrderItemBrief.class
                , MfhApplication.getAppContext()) {
        };

        InvSendOrderApiImpl.getById(id, responseCallback);
    }
}
