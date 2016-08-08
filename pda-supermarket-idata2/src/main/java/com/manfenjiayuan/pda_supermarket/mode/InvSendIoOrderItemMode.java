package com.manfenjiayuan.pda_supermarket.mode;

import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * 采购订单明细
 * Created by bingshanguxue on 16/3/17.
 */
public class InvSendIoOrderItemMode implements IInvSendIoOrderItemMode<InvSendIoOrderItemBrief> {
    /**
     * 适用场景，查看发货单
     * */
    @Override
    public void loadOrderItemsByBarcode(String barcode,
                                        final OnModeListener<InvSendIoOrderItemBrief> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)){
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<InvSendIoOrderItemBrief,
                NetProcessor.Processor<InvSendIoOrderItemBrief>>(
                new NetProcessor.Processor<InvSendIoOrderItemBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {

                        InvSendIoOrderItemBrief invSendIoOrderItemBrief = null;

                        if (rspData != null) {
                            //com.mfh.comn.net.data.EntityWrapper cannot be cast to com.mfh.comn.net.data.RspBean
                            EntityWrapper<InvSendIoOrderItemBrief> retValue = (EntityWrapper<InvSendIoOrderItemBrief>) rspData;
                            invSendIoOrderItemBrief = retValue.getBean();
                        }

                        if (listener != null) {
                            listener.onSuccess(invSendIoOrderItemBrief);
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
                , InvSendIoOrderItemBrief.class
                , MfhApplication.getAppContext()) {
        };

        InvSendIoOrderApiImpl.getInvSendIoOrderByBarcode(barcode, responseCallback);
    }

}
