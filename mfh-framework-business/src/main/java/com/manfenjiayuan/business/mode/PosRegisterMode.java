package com.manfenjiayuan.business.mode;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.impl.MfhApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * {@link InvSkuGoods}
 * Created by bingshanguxue on 16/3/17.
 */
public class PosRegisterMode {

    public void create(String channelId, String channelPointId, Long netId, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("Initialize--get terminal id success:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                            if (listener != null) {
                                listener.onSuccess(retStr);
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("Initialize--get terminal id failed(%s),please set manual", errMsg));
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("serialNo", MfhApplication.getWifiMac15Bit());
        order.put("channelId", channelId);
        order.put("channelPointId", channelPointId);
        order.put("netId", netId);

        ZLogger.df("Initialize--register terminal id," + order.toJSONString());
        MfhApiImpl.posRegisterCreate(order.toJSONString(), responseCallback);
    }

    public void update(String terminalId, String channelId, String channelPointId, Long netId, final OnModeListener<String> listener) {
        if (listener != null) {
            listener.onProcess();
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();
                            ZLogger.df("Initialize--get terminal id success:" + retStr);
                            SharedPreferencesManager.setTerminalId(retStr);
                            if (listener != null) {
                                listener.onSuccess(retStr);
                            }
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df(String.format("Initialize--get terminal id failed(%s),please set manual", errMsg));
                        if (listener != null) {
                            listener.onError(errMsg);
                        }
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        JSONObject order = new JSONObject();
        order.put("id", terminalId);
        order.put("channelId", channelId);
        order.put("channelPointId", channelPointId);
        order.put("netId", netId);

        ZLogger.df("Initialize--register terminal id," + order.toJSONString());
        MfhApiImpl.posRegisterUpdate(order.toJSONString(), responseCallback);
    }

}
