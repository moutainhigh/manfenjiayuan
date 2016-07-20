package com.manfenjiayuan.business.mode;

import com.manfenjiayuan.business.bean.InvFindOrderItemBrief;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * 拣货单明细
 * Created by bingshanguxue on 16/3/17.
 */
public class InvFindOrderMode  {
    /**
     * 适用场景，批发商根据拣货单发货，扫描拣货单条码，快速录入数量
     * */
    public void loadOrderItemsByBarcode(String barcode, final OnModeListener<InvFindOrderItemBrief> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)){
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<InvFindOrderItemBrief,
                NetProcessor.Processor<InvFindOrderItemBrief>>(
                new NetProcessor.Processor<InvFindOrderItemBrief>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData == null) {
                            if (listener != null) {
                                listener.onSuccess(null);
                            }
                            return;
                        }
                        //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<InvFindOrderItemBrief> retValue = (RspBean<InvFindOrderItemBrief>) rspData;
                        if (listener != null) {
                            listener.onSuccess(retValue.getValue());
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
                , InvFindOrderItemBrief.class
                , MfhApplication.getAppContext()) {
        };

        InvOrderApiImpl.getInvFindOrderByBarcode(barcode, null, responseCallback);
    }

}
