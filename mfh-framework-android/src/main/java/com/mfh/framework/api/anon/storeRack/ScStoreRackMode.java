package com.mfh.framework.api.anon.storeRack;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

/**
 * Created by bingshanguxue on 8/30/16.
 */
public class ScStoreRackMode {
    /**
     * 查询货架
     * */
    public void getByShopIdMust(Long shopId, final OnModeListener<Long> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Long,
                NetProcessor.Processor<Long>>(
                new NetProcessor.Processor<Long>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                        try {
                            RspBean<Long> retValue = (RspBean<Long>) rspData;
                            if (listener != null) {
                                listener.onSuccess(retValue.getValue());
                            }
                        }
                        catch (Exception e){
                            ZLogger.ef(e.toString());
                            if (listener != null) {
                                listener.onError(e.getMessage());
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载店铺信息失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , Long.class
                , MfhApplication.getAppContext()) {
        };

        ScStoreRackApi.getByShopIdMust(shopId, responseCallback);
    }

    /**
     * 查询货架商品信息
     * */
    public void getById(Long rackId, final OnModeListener<StoreRack> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<StoreRack,
                NetProcessor.Processor<StoreRack>>(
                new NetProcessor.Processor<StoreRack>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                        StoreRack storeRack = null;
                        try {
                            RspBean<StoreRack> retValue = (RspBean<StoreRack>) rspData;
                            storeRack = retValue.getValue();
                        }
                        catch (Exception e){
                            ZLogger.ef(e.toString());
                        }

                        if (listener != null) {
                            listener.onSuccess(storeRack);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载店铺信息失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , StoreRack.class
                , MfhApplication.getAppContext()) {
        };

        ScStoreRackApi.getById(rackId, responseCallback);
    }

    /**
     * 查询货架
     * */
    public void getByShopMust(Long shopId, final OnModeListener<Long> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<Long,
                NetProcessor.Processor<Long>>(
                new NetProcessor.Processor<Long>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","dat"}}
                        try {
                            RspBean<Long> retValue = (RspBean<Long>) rspData;
                            if (listener != null) {
                                listener.onSuccess(retValue.getValue());
                            }
                        }
                        catch (Exception e){
                            ZLogger.ef(e.toString());
                            if (listener != null) {
                                listener.onError(e.getMessage());
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("加载店铺信息失败:" + errMsg);
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , Long.class
                , MfhApplication.getAppContext()) {
        };

        ScStoreRackApi.getByShopMust(shopId, responseCallback);
    }

}
