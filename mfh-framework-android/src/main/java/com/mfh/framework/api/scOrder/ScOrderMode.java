package com.mfh.framework.api.scOrder;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

/**
 * Created by bingshanguxue on 9/22/16.
 */

public class ScOrderMode {
    /**
     * 查询商品
     */
    public void getByCode(String barcode, final OnModeListener<ScOrder> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        if (StringUtils.isEmpty(barcode)) {
            if (listener != null) {
                listener.onError("缺少barcode参数");
            }
            return;
        }

        NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScOrder,
                NetProcessor.Processor<ScOrder>>(
                new NetProcessor.Processor<ScOrder>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        // {"code":"0","msg":"查询成功!","version":"1","data":null}
                        ScOrder scOrder = null;
                        if (rspData != null) {
                            RspBean<ScOrder> retValue = (RspBean<ScOrder>) rspData;
                            scOrder = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(scOrder);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询失败: " + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , ScOrder.class
                , MfhApplication.getAppContext()) {
        };

        ScOrderApi.getByCode(barcode, queryResCallback);
    }

}
