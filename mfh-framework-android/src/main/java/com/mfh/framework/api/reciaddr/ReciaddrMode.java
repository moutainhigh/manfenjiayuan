package com.mfh.framework.api.reciaddr;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.mvp.OnPageModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

/**
 * Created by bingshanguxue on 09/10/2016.
 */

public class ReciaddrMode {

    /**
     * 查询收货地址
     * @param humanId 人员编号
     * */
    public void getAllAddrsByHuman(Long humanId,
                                   final OnPageModeListener<Reciaddr> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Reciaddr,
                NetProcessor.Processor<Reciaddr>>(
                new NetProcessor.Processor<Reciaddr>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
//                        {"code":"0","msg":"查询成功!","version":"1","data":[6.0,6.0]}
                        RspListBean<Reciaddr> retValue = (RspListBean<Reciaddr>) rspData;
                        if (listener != null) {
                            listener.onSuccess(null, retValue.getValue());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载收货地址失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , Reciaddr.class
                , MfhApplication.getAppContext()) {
        };

        ReciaddrApi.getAllAddrsByHuman(humanId, responseCallback);
    }

    /**
     * 查询默认收货地址
     * @param humanId 人员编号
     * */
    public void getDefaultAddrsByHuman(Long humanId,
                                   final OnModeListener<Reciaddr> listener) {
        if (listener != null) {
            listener.onProcess();
        }
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Reciaddr,
                NetProcessor.Processor<Reciaddr>>(
                new NetProcessor.Processor<Reciaddr>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
//                        {"code":"0","msg":"查询成功!","version":"1","data":[6.0,6.0]}
                        Reciaddr reciaddr = null;
                        if (rspData != null){
                            RspBean<Reciaddr> retValue = (RspBean<Reciaddr>) rspData;
                            reciaddr = retValue.getValue();
                        }
                        if (listener != null) {
                            listener.onSuccess(reciaddr);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载默认收货地址失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , Reciaddr.class
                , MfhApplication.getAppContext()) {
        };

        ReciaddrApi.getDefaultAddrsByHuman(humanId, responseCallback);
    }
}
