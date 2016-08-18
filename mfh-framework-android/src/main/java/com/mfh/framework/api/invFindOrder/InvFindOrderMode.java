package com.mfh.framework.api.invFindOrder;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.net.data.IResponseData;
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
                        try{
                            InvFindOrderItemBrief data = null;
                            if (rspData != null) {
                                //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                                EntityWrapper<InvFindOrderItemBrief> retValue = (EntityWrapper<InvFindOrderItemBrief>) rspData;
                                data = retValue.getBean();
                            }
                            if (listener != null) {
                                listener.onSuccess(data);
                            }
                        }
                        catch (Exception e){
                            if (listener != null) {
                                listener.onError("解析数据失败:" + e.toString());
                            }
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

        InvOrderApiImpl.getInvFindOrderByBarcode(barcode, responseCallback);
    }

}
